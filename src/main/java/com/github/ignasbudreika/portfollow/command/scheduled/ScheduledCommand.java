package com.github.ignasbudreika.portfollow.command.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class ScheduledCommand {
    @Scheduled(cron = "*/30 * * * * *")
    public void doScheduledCommand() {
        log.info("Doing scheduled command. Current time: {}", LocalDateTime.now());
    }
}
