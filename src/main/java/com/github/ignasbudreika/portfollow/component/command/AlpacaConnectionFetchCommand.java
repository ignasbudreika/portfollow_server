package com.github.ignasbudreika.portfollow.component.command;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.AlpacaService;
import com.github.ignasbudreika.portfollow.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class AlpacaConnectionFetchCommand {
    @Autowired
    private UserService userService;
    @Autowired
    private AlpacaService alpacaService;

    @Scheduled(cron = "0 8 * * * *")
    public void fetchAlpacaConnections() {
        log.info("fetching Alpaca connections. Current time: {}", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Iterable<User> users = userService.getAll();

        users.forEach(user -> {
            try {
                log.info("fetching user: {} Alpaca connections", user.getId());
                alpacaService.fetchPositions(user);
            } catch (Exception e) {
                log.error("failed to fetch Alpaca connections for user: {}", user.getId(), e);
            }
        });
    }
}
