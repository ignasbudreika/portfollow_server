package com.github.ignasbudreika.portfollow.command.scheduled;

import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.SpectroCoinService;
import com.github.ignasbudreika.portfollow.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class SpectroCoinConnectionsFetchCommand {
    @Autowired
    private SpectroCoinService spectroCoinService;
    @Autowired
    private UserService userService;

    @Scheduled(cron = "0 0 */6 * * *")
    public void fetchSpectroCoinConnections() {
        log.info("fetching SpectroCoin connections. Current time: {}", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Iterable<User> users = userService.getAll();

        users.forEach(user -> {
            try {
                log.info("fetching user: {} SpectroCoin connection", user.getId());
                spectroCoinService.fetchCryptocurrencies(user);
            } catch (Exception e) {
                log.error("failed to fetch SpectroCoin connection for user: {}", user.getId(), e);
            }
        });
    }
}