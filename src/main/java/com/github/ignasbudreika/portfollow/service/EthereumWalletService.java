package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.EthereumWalletConnectionDTO;
import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.external.helper.EthereumWalletHelper;
import com.github.ignasbudreika.portfollow.model.EthereumWalletConnection;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.EthereumWalletConnectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
public class EthereumWalletService {
    @Autowired
    private EthereumWalletConnectionRepository connectionRepository;
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private EthereumWalletHelper walletHelper;

    public void addConnection(EthereumWalletConnectionDTO connectionDTO, User user) throws Exception {
        if (connectionRepository.findByAddress(connectionDTO.getAddress()) != null) {
            throw new EntityExistsException(String.format(
                    "Ethereum wallet connection of address: {} for user: %s already exists", connectionDTO.getAddress(), user.getId()
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

    @Transactional
    public void removeConnection(String id) {
        EthereumWalletConnection connection = connectionRepository.findById(id).orElseThrow();

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

    @Transactional
    public void fetchBalance(EthereumWalletConnection connection, User user) {
        try {
            BigDecimal etherQuantity = walletHelper.getWalletBalanceInEther(connection.getAddress());

            investmentService.saveInvestmentFetchedFromConnection(Investment.builder()
                    .symbol("ETH")
                    .quantity(etherQuantity.setScale(8, RoundingMode.HALF_UP))
                    .type(InvestmentType.CRYPTOCURRENCY)
                    .user(user).build(), connection.getId());

            log.info("imported Ethereum balance for user: {} from address: {}, balance: {}",
                    user.getId(), connection.getAddress(), etherQuantity);
        } catch (IOException e) {
            log.error("error occurred while fetching balance for user: {} from Ethereum address: {}",
                    user.getId(), connection.getAddress(), e);
        }

        connection.setLastFetched(LocalDateTime.now());
        connectionRepository.save(connection);
    }
}
