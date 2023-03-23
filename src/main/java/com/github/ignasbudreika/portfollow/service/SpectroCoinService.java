package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.SpectroCoinConnectionDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.external.client.SpectroCoinClient;
import com.github.ignasbudreika.portfollow.external.dto.response.AccountsDTO;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.SpectroCoinConnection;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.SpectroCoinConnectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

@Slf4j
@Service
public class SpectroCoinService {
    private static final Set<String> SUPPORTED_CRYPTOCURRENCIES = Set.of("BTC", "ETH", "SHIB", "USDT");

    @Autowired
    private SpectroCoinConnectionRepository spectroCoinConnectionRepository;
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private SpectroCoinClient spectroCoinClient;

    public void addConnection(com.github.ignasbudreika.portfollow.api.dto.request.SpectroCoinConnectionDTO connectionDTO, User user) throws Exception {
        if (spectroCoinConnectionRepository.findByUserId(user.getId()) != null) {
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
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findByUserId(userId);
        if (connection == null) {
            return null;
        }

        return SpectroCoinConnectionDTO.builder()
                .clientId(connection.getClientId())
                .lastFetched(connection.getLastFetched())
                .status(connection.getStatus()).build();
    }

    private SpectroCoinConnection getActiveConnectionOrThrowException(String userId) throws BusinessLogicException {
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findByUserId(userId);
        if (connection == null || !connection.getStatus().equals(ConnectionStatus.ACTIVE)) {
            throw new EntityNotFoundException(String.format("no active SpectroCoin connection found for user: %s", userId));
        }

        return connection;
    }

    @Transactional
    public void removeConnection(String id) {
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findById(id).orElseThrow();

        connection.setStatus(ConnectionStatus.INACTIVE);
        connection.setClientSecret(null);

        spectroCoinConnectionRepository.save(connection);
    }

    @Transactional
    public void invalidateConnection(String id) {
        SpectroCoinConnection connection = spectroCoinConnectionRepository.findById(id).orElseThrow();

        connection.setStatus(ConnectionStatus.INVALID);
        connection.setClientSecret(null);

        spectroCoinConnectionRepository.save(connection);
    }

    @Transactional
    public void fetchCryptocurrencies(User user) {
        try {
            SpectroCoinConnection connection = getActiveConnectionOrThrowException(user.getId());

            AccountsDTO accounts = spectroCoinClient.getAccountData(connection.getClientId(), connection.getClientSecret());

            Arrays.stream(accounts.getAccounts()).forEach(account -> {
                if (SUPPORTED_CRYPTOCURRENCIES.contains(account.getCurrencyCode())
                        && account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                    investmentService.saveInvestmentFetchedFromConnection(Investment.builder()
                            .symbol(account.getCurrencyCode())
                            .quantity(account.getBalance().setScale(8, RoundingMode.HALF_UP))
                            .type(InvestmentType.CRYPTOCURRENCY)
                            .user(user).build(), connection.getId());

                    log.info("imported {} cryptocurrency for user: {} from SpectroCoin, balance: {}",
                            account.getCurrencyCode(), user.getId(), account.getBalance());
                }
            });

            connection.setLastFetched(LocalDateTime.now());
            spectroCoinConnectionRepository.save(connection);
        } catch (EntityNotFoundException e) {
            log.info("could not fetch cryptocurrencies for user: {} because no active SpectroCoin connection exists", user.getId());
        } catch (Exception e) {
            log.error("error occurred while fetching cryptocurrencies for user: {} from SpectroCoin", user.getId(), e);
        }
    }
}
