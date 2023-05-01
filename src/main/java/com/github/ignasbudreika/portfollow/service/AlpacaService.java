package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CreateAlpacaConnectionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.AlpacaConnectionDTO;
import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType;
import com.github.ignasbudreika.portfollow.exception.InvalidExternalRequestException;
import com.github.ignasbudreika.portfollow.external.client.AlpacaClient;
import com.github.ignasbudreika.portfollow.external.dto.response.PositionDTO;
import com.github.ignasbudreika.portfollow.model.AlpacaConnection;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.AlpacaConnectionRepository;
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
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AlpacaService {
    private static final String US_EQUITY = "us_equity";

    private AlpacaConnectionRepository alpacaConnectionRepository;
    private InvestmentService investmentService;
    private AlpacaClient alpacaClient;

    public void addConnection(CreateAlpacaConnectionDTO connectionDTO, User user) throws Exception {
        if (alpacaConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE) != null) {
            throw new EntityExistsException(String.format(
                    "Alpaca connection for user: %s already exists", user.getId()
            ));
        }

        AlpacaConnection connection = AlpacaConnection.builder()
                .apiKey(connectionDTO.getApiKey())
                .secret(connectionDTO.getSecret())
                .user(user)
                .status(ConnectionStatus.ACTIVE).build();

        alpacaConnectionRepository.save(connection);

        log.info("successfully added Alpaca connection for user: {}, fetching cryptocurrencies", user.getId());

        fetchPositions(user);
    }

    public AlpacaConnectionDTO getConnection(String userId) {
        AlpacaConnection connection = alpacaConnectionRepository.findByUserIdAndStatus(userId, ConnectionStatus.ACTIVE);
        if (connection == null) {
            return AlpacaConnectionDTO.builder().status(ConnectionStatus.INACTIVE).build();
        }

        return AlpacaConnectionDTO.builder()
                .apiKey(connection.getApiKey())
                .lastFetched(connection.getLastFetched().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .status(connection.getStatus()).build();
    }

    private AlpacaConnection getActiveConnectionOrThrowException(String userId) {
        AlpacaConnection connection = alpacaConnectionRepository.findByUserIdAndStatus(userId, ConnectionStatus.ACTIVE);
        if (connection == null || !connection.getStatus().equals(ConnectionStatus.ACTIVE)) {
            throw new EntityNotFoundException(String.format("no active Alpaca connection found for user: %s", userId));
        }

        return connection;
    }

    public void removeConnection(User user) {
        AlpacaConnection connection = alpacaConnectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE);
        if (connection == null) {
            return;
        }

        investmentService.deleteConnection(connection.getId());

        connection.setStatus(ConnectionStatus.INACTIVE);
        connection.setSecret(null);

        alpacaConnectionRepository.save(connection);
    }

    public void invalidateConnection(String id) {
        AlpacaConnection connection = alpacaConnectionRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        investmentService.deleteConnection(connection.getId());

        connection.setStatus(ConnectionStatus.INVALID);
        connection.setSecret(null);

        alpacaConnectionRepository.save(connection);
    }

    public void fetchPositions(User user) {
        try {
            AlpacaConnection connection = getActiveConnectionOrThrowException(user.getId());

            try {
                List<PositionDTO> positions = alpacaClient.getPositions(connection.getApiKey(), connection.getSecret());

                positions.forEach(position -> {
                    if (US_EQUITY.equals(position.getAssetClass())) {
                        try {
                            investmentService.saveInvestmentFetchedFromConnection(Investment.builder()
                                    .symbol(position.getSymbol())
                                    .quantity(new BigDecimal(position.getQuantity()).setScale(8, RoundingMode.HALF_UP))
                                    .type(InvestmentType.STOCK)
                                    .updateType(InvestmentUpdateType.ALPACA)
                                    .date(LocalDate.now())
                                    .user(user).build(), connection.getId());

                            log.info("imported {} stock for user: {} from Alpaca, balance: {}",
                                    position.getSymbol(), user.getId(), position.getQuantity());
                        } catch (Exception e) {
                            log.error("failed to import: {} symbol for user: {}", position.getSymbol(), user.getId(), e);
                        }
                    }
                });

                connection.setLastFetched(LocalDateTime.now());
                alpacaConnectionRepository.save(connection);
            } catch (InvalidExternalRequestException e) {
                log.warn("failed to fetch stocks from Alpaca for user: {}", user.getId(), e);

                log.info("invalidating Alpaca connection: {} for user: {}", connection.getId(), user.getId());
                invalidateConnection(connection.getId());
            } catch (Exception e) {
                log.error("error occurred while fetching stocks for user: {} from Alpaca", user.getId(), e);
            }
        } catch (EntityNotFoundException e) {
            log.info("could not fetch stocks for user: {} because no active Alpaca connection exists", user.getId());
        }
    }
}
