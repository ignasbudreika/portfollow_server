package com.github.ignasbudreika.portfollow.command.scheduled;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.EthereumWalletService;
import com.github.ignasbudreika.portfollow.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class EthereumWalletConnectionFetchCommand {
    @Autowired
    private UserService userService;
    @Autowired
    private EthereumWalletService walletService;

    @Scheduled(cron = "0 10 */6 * * *")
    public void fetchSpectroCoinConnections() {
        log.info("fetching Ethereum wallet connections. Current time: {}", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Iterable<User> users = userService.getAll();

        users.forEach(user -> {
            try {
                log.info("fetching user: {} Ethereum wallet connections", user.getId());
                walletService.fetchBalances(user);
            } catch (Exception e) {
                log.error("failed to fetch Ethereum wallet connections for user: {}", user.getId(), e);
            }
        });
    }
}
