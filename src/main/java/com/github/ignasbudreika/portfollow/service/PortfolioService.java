package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;

@Service
@AllArgsConstructor
public class PortfolioService {
    private PortfolioRepository portfolioRepository;
    private PortfolioHistoryService historyService;

    public Portfolio createPortfolio(User user) {
        if (portfolioRepository.existsByUserId(user.getId())) {
            throw new EntityExistsException("user: {} already has a portfolio");
        }

        Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
                .user(user)
                .title(String.format("%s's portfolio", user.getEmail()))
                .hiddenValue(true)
                .published(false).build());

        historyService.initPortfolio(user);

        return portfolio;
    }
}
