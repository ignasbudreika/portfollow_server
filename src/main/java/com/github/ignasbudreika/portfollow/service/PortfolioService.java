package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PortfolioService {
    @Autowired
    private PortfolioRepository portfolioRepository;

    public Portfolio savePortfolio(String userId, BigDecimal value, LocalDateTime date) {
        return portfolioRepository.save(Portfolio.builder()
                .user(User.builder().id(userId).build())
                .value(value)
                .date(date).build());
    }
}
