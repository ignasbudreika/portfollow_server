package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CreateSpectroCoinConnectionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.SpectroCoinConnectionDTO;
import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.InvalidExternalRequestException;
import com.github.ignasbudreika.portfollow.external.client.SpectroCoinClient;
import com.github.ignasbudreika.portfollow.external.dto.response.AccountsDTO;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.SpectroCoinConnection;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.SpectroCoinConnectionRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class SpectroCoinService {
    private static final List<String> SUPPORTED_CRYPTOCURRENCIES = List.of("BTC","ETH","APT","SAND","MASK",
            "EUROC","BUSD","BNB","DASH","XEM","LTC","XLM","XRP","ZEC","BNK","USDC","TUSD","USDP","wBTC","LINK","DAI",
            "UNI","MKR","AAVE","COMP","ZRX","BAT","SUSHI","ALGO","DOGE","SHIB","MATIC","MANA","UMA","HOT","CHZ","GRT",
            "SNX","ENJ","ADA","SOL","DOT","FIL","VET","ZIL","MIOTA","ONT","ATOM","EOS","TRX","CGLD","NEO","XTZ","1INCH","USDT");

    private SpectroCoinConnectionRepository spectroCoinConnectionRepository;
    private InvestmentService investmentService;
    private SpectroCoinClient spectroCoinClient;

    public void addConnection(CreateSpectroCoinConnectionDTO connectionDTO, User user) throws Exception {
        if (spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE) != null) {
            throw new EntityExistsException(String.format(
                    "SpectroCoin connection for user: %s already exists", user.getId()
            ));
        }

        if (!spectroCoinClient.credentialsAreValid(connectionDTO.getClientId(), connectionDTO.getClientSecret())) {
            throw new BusinessLogicException(String.format("invalid SpectroCoin wallet API credentials for user: %s", user.getId()));
        }

        SpectroCoinConnection connection = SpectroCoinConnection.builder()
                .clientId(connectionDTO.getClientId())
                .clientSecret(connectionDTO.getClientSecret())
                .user(user)
                .status(ConnectionStatus.ACTIVE).build();

        spectroCoinConnectionRepository.save(connection);

        log.info("successfully added SpectroCoin connection for user: {}, fetching cryptocurrencies", user.getId());

        fetchCryptocurrencies(user);
    }

    public SpectroCoinConnectionDTO getConnection(String userId) {
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findByUserIdAndStatus(userId, ConnectionStatus.ACTIVE);
        if (connection == null) {
            return SpectroCoinConnectionDTO.builder().status(ConnectionStatus.INACTIVE).build();
        }

        return SpectroCoinConnectionDTO.builder()
                .clientId(connection.getClientId())
                .lastFetched(connection.getLastFetched().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .status(connection.getStatus()).build();
    }

    private SpectroCoinConnection getActiveConnectionOrThrowException(String userId) {
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findByUserIdAndStatus(userId, ConnectionStatus.ACTIVE);
        if (connection == null || !connection.getStatus().equals(ConnectionStatus.ACTIVE)) {
            throw new EntityNotFoundException(String.format("no active SpectroCoin connection found for user: %s", userId));
        }

        return connection;
    }

    public void removeConnection(User user) {
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE);
        if (connection == null) {
            return;
        }

        investmentService.deleteConnection(connection.getId());

        connection.setStatus(ConnectionStatus.INACTIVE);
        connection.setClientSecret(null);

        spectroCoinConnectionRepository.save(connection);
    }

    public void invalidateConnection(String id) {
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        investmentService.deleteConnection(connection.getId());

        connection.setStatus(ConnectionStatus.INVALID);
        connection.setClientSecret(null);

        spectroCoinConnectionRepository.save(connection);
    }

    public void fetchCryptocurrencies(User user) {
        try {
            SpectroCoinConnection connection = getActiveConnectionOrThrowException(user.getId());

            try {
                AccountsDTO accounts = spectroCoinClient.getAccountData(connection.getClientId(), connection.getClientSecret());

                Arrays.stream(accounts.getAccounts()).forEach(account -> {
                    if (SUPPORTED_CRYPTOCURRENCIES.contains(account.getCurrencyCode())
                            && account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                        try {
                            investmentService.saveInvestmentFetchedFromConnection(Investment.builder()
                                    .symbol(account.getCurrencyCode())
                                    .quantity(account.getBalance().setScale(8, RoundingMode.HALF_UP))
                                    .type(InvestmentType.CRYPTO)
                                    .updateType(InvestmentUpdateType.SPECTROCOIN)
                                    .date(LocalDate.now())
                                    .user(user).build(), connection.getId());

                            log.info("imported {} cryptocurrency for user: {} from SpectroCoin, balance: {}",
                                    account.getCurrencyCode(), user.getId(), account.getBalance());
                        } catch (Exception e) {
                            log.error("failed to import: {} symbol for user: {}", account.getCurrencyCode(), user.getId(), e);
                        }
                    }
                });

                connection.setLastFetched(LocalDateTime.now());
                spectroCoinConnectionRepository.save(connection);
            } catch (InvalidExternalRequestException e) {
                log.warn("failed to fetch cryptocurrencies from SpectroCoin for user: {}", user.getId(), e);

                log.info("invalidating SpectroCoin connection: {} for user: {}", connection.getId(), user.getId());
                invalidateConnection(connection.getId());
            } catch (Exception e) {
                log.error("error occurred while fetching cryptocurrencies for user: {} from SpectroCoin", user.getId(), e);
            }
        } catch (EntityNotFoundException e) {
            log.info("could not fetch cryptocurrencies for user: {} because no active SpectroCoin connection exists", user.getId());
        }
    }
}
