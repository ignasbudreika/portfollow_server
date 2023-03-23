package com.github.ignasbudreika.portfollow.command.scheduled;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class PortfolioValueCollectorCommand {
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private SpectroCoinService spectroCoinService;
    @Autowired
    private UserService userService;
    @Autowired
    private PortfolioService portfolioService;
    @Autowired
    private EthereumWalletService walletService;

    @Scheduled(cron = "0 0 */6 * * *")
    public void retrievePortfolioValues() {
        log.info("Updating portfolios values. Current time: {}", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Iterable<User> users = userService.getAll();

        users.forEach(user -> {
            // todo move into separate command that happens earlier
            log.info("Fetching user: {} SpectroCoin connection", user.getId());
            spectroCoinService.fetchCryptocurrencies(user);

            log.info("Fetching user: {} Ethereum wallet connections", user.getId());
            walletService.fetchBalances(user);

            log.info("Calculating user: {} portfolio value", user.getId());
            BigDecimal totalValue = investmentService.getTotalValueByUserId(user.getId());

            log.info("Saving user: {} portfolio value: {}", user.getId(), totalValue);
            portfolioService.savePortfolio(user.getId(), totalValue, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        });
    }
}
