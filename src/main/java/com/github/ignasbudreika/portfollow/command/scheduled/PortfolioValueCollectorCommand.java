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
    private UserService userService;
    @Autowired
    private PortfolioService portfolioService;

    @Scheduled(cron = "0 20 */6 * * *")
    public void retrievePortfolioValues() {
        log.info("updating portfolios values. Current time: {}", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Iterable<User> users = userService.getAll();

        users.forEach(user -> {
            try {
                log.info("calculating user: {} portfolio value", user.getId());
                BigDecimal totalValue = investmentService.getTotalValueByUserId(user.getId());

                log.info("saving user: {} portfolio value: {}", user.getId(), totalValue);
                portfolioService.savePortfolio(user.getId(), totalValue, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            } catch (Exception e) {
                log.error("failed to update total portfolio value for user: {}", user.getId(), e);
            }
        });
    }
}
