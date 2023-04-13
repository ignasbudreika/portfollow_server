package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioInfoDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.SettingsDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.UserInfoDTO;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    @Autowired
    private PortfolioRepository portfolioRepository;

    public SettingsDTO getUserSettings(User user) {
        if (!user.isSetup()) {
            return SettingsDTO.builder()
                    .setupNeeded(true)
                    .userInfo(UserInfoDTO.builder()
                            .email(user.getEmail())
                            .username(user.getUsername()).build())
                    .build();
        }

        Portfolio portfolio = portfolioRepository.findByUserId(user.getId());
        if (portfolio == null) {
            throw new EntityNotFoundException();
        }

        return SettingsDTO.builder()
                .setupNeeded(false)
                .userInfo(UserInfoDTO.builder()
                        .email(user.getEmail())
                        .username(user.getUsername()).build())
                .portfolioInfo(PortfolioInfoDTO.builder()
                        .description(portfolio.getDescription())
                        .isPublic(portfolio.isPublished())
                        .revealValue(!portfolio.isHiddenValue())
                        .currencyEur(portfolio.isCurrencyEur())
                        .allowedUsers(portfolio.getAllowedUsers() == null ?
                                new String[]{} :
                                portfolio.getAllowedUsers().split(",")).build())
                .build();
    }
}
