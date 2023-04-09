package com.github.ignasbudreika.portfollow.command.scheduled;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private PortfolioHistoryService portfolioHistoryService;

    @Scheduled(cron = "0 20 * * * *")
    public void retrievePortfolioValues() {
        log.info("updating portfolios values. Current time: {}", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Iterable<User> users = userService.getAll();

        users.forEach(user -> {
            try {
                log.info("updating user: {} portfolio value", user.getId());
                portfolioHistoryService.saveCurrentPortfolio(user.getId());
            } catch (Exception e) {
                log.error("failed to update total portfolio value for user: {}", user.getId(), e);
            }
        });
    }
}
