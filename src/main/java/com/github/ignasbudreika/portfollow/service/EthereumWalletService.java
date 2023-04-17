package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.EthereumWalletConnectionDTO;
import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.external.helper.EthereumWalletHelper;
import com.github.ignasbudreika.portfollow.model.EthereumWalletConnection;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.EthereumWalletConnectionRepository;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class EthereumWalletService {
    private static final String ETHEREUM = "ETH";

    @Autowired
    private EthereumWalletConnectionRepository connectionRepository;
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private EthereumWalletHelper walletHelper;

    public void addConnection(EthereumWalletConnectionDTO connectionDTO, User user) throws Exception {
        if (connectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE) != null) {
            throw new EntityExistsException(String.format(
                    "Ethereum wallet connection for user: %s already exists", user.getId()
            ));
        }

        EthereumWalletConnection connection = EthereumWalletConnection.builder()
                .address(connectionDTO.getAddress())
                .user(user)
                .status(ConnectionStatus.ACTIVE).build();

        connectionRepository.save(connection);

        log.info("successfully added Ethereum wallet connection for user: {}, fetching balance", user.getId());

        fetchBalance(connection, user);
    }

    public com.github.ignasbudreika.portfollow.api.dto.response.EthereumWalletConnectionDTO getConnectionByUserId(String userId) {
        EthereumWalletConnection connection = connectionRepository.findByUserIdAndStatus(userId, ConnectionStatus.ACTIVE);
        if (connection == null) {
            return com.github.ignasbudreika.portfollow.api.dto.response.EthereumWalletConnectionDTO.builder()
                    .status(ConnectionStatus.INACTIVE).build();
        }

        return com.github.ignasbudreika.portfollow.api.dto.response.EthereumWalletConnectionDTO.builder()
                .address(connection.getAddress())
                .lastFetched(connection.getLastFetched().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .status(connection.getStatus()).build();
    }

    @Transactional
    public void removeConnection(User user) {
        EthereumWalletConnection connection = connectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.ACTIVE);
        if (connection == null) {
            return;
        }

        connection.setStatus(ConnectionStatus.INACTIVE);

        connectionRepository.save(connection);
    }

    @Transactional
    public void invalidateConnection(String id) {
        EthereumWalletConnection connection = connectionRepository.findById(id).orElseThrow();

        connection.setStatus(ConnectionStatus.INVALID);

        connectionRepository.save(connection);
    }

    @Transactional
    public void fetchBalances(User user) {
        connectionRepository.findAllByUserId(user.getId()).forEach(connection -> {
            fetchBalance(connection, user);
        });
    }

    public void fetchBalance(EthereumWalletConnection connection, User user) {
        try {
            BigDecimal etherQuantity = walletHelper.getWalletBalanceInEther(connection.getAddress());

            investmentService.saveInvestmentFetchedFromConnection(Investment.builder()
                    .symbol(ETHEREUM)
                    .quantity(etherQuantity.setScale(8, RoundingMode.HALF_UP))
                    .type(InvestmentType.CRYPTOCURRENCY)
                    .date(LocalDate.now())
                    .user(user).build(), connection.getId());

            log.info("imported Ethereum balance for user: {} from address: {}, balance: {}",
                    user.getId(), connection.getAddress(), etherQuantity);
        } catch (Exception e) {
            log.error("error occurred while fetching balance for user: {} from Ethereum address: {}",
                    user.getId(), connection.getAddress(), e);
        }

        connection.setLastFetched(LocalDateTime.now());
        connectionRepository.save(connection);
    }
}
