package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;

@Service
public class PortfolioService {
    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private PortfolioHistoryService historyService;

    public Portfolio createPortfolio(User user) {
        if (portfolioRepository.existsByUserId(user.getId())) {
            throw new EntityExistsException("user: {} already has a portfolio");
        }

        Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
                .user(user)
                .hiddenValue(true)
                .published(false).build());

        historyService.initPortfolio(portfolio);

        return portfolio;
    }
}
