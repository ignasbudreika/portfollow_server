package com.github.ignasbudreika.portfollow.component.command;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.InvestmentService;
import com.github.ignasbudreika.portfollow.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class PeriodicInvestmentFetchCommand {
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private UserService userService;

    @Scheduled(cron = "0 8 0 * * *")
    public void fetchPeriodicInvestments() {
        log.info("fetching periodic investments. Current time: {}", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Iterable<User> users = userService.getAll();
        LocalDate date = LocalDate.now();

        users.forEach(user -> {
            try {
                log.info("fetching user: {} periodic investments", user.getId());

                investmentService.fetchPeriodicInvestments(user, date);
            } catch (Exception e) {
                log.error("failed to fetch periodic investments for user: {}", user.getId(), e);
            }
        });
    }
}
