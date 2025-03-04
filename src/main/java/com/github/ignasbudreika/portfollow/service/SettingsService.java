package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.SettingsUpdateDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioInfoDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.SettingsDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.UserInfoDTO;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import com.github.ignasbudreika.portfollow.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SettingsService {
    private PortfolioRepository portfolioRepository;
    private UserRepository userRepository;

    public SettingsDTO getUserSettings(User user) {
        Portfolio portfolio = portfolioRepository.findByUserId(user.getId());

        return SettingsDTO.builder()
                .userInfo(UserInfoDTO.builder()
                        .email(user.getEmail())
                        .username(user.getUsername()).build())
                .portfolioInfo(PortfolioInfoDTO.builder()
                        .id(portfolio.getId())
                        .title(portfolio.getTitle())
                        .description(portfolio.getDescription())
                        .isPublic(portfolio.isPublished())
                        .revealValue(!portfolio.isHiddenValue())
                        .allowedUsers(portfolio.getAllowedUsers() == null ?
                                new String[]{} :
                                portfolio.getAllowedUsers().split(",")).build())
                .build();
    }

    public SettingsDTO updateUserSettings(SettingsUpdateDTO settings, User user) {
        Portfolio portfolio = portfolioRepository.findByUserId(user.getId());

        if (StringUtils.isNotBlank(settings.getUsername())) {
            user.setUsername(settings.getUsername());
            userRepository.save(user);
        }
        if (StringUtils.isNotBlank(settings.getTitle())) {
            portfolio.setTitle(settings.getTitle());
        }
        portfolio.setDescription(settings.getDescription());
        portfolio.setPublished(settings.isPublic());
        portfolio.setHiddenValue(settings.isHideValue());
        portfolio.setAllowedUsers(settings.getAllowedUsers());

        portfolioRepository.save(portfolio);

        return SettingsDTO.builder()
                .userInfo(UserInfoDTO.builder()
                        .email(user.getEmail())
                        .username(user.getUsername()).build())
                .portfolioInfo(PortfolioInfoDTO.builder()
                        .id(portfolio.getId())
                        .title(portfolio.getTitle())
                        .description(portfolio.getDescription())
                        .isPublic(portfolio.isPublished())
                        .revealValue(!portfolio.isHiddenValue())
                        .allowedUsers(portfolio.getAllowedUsers() == null ?
                                new String[]{} :
                                portfolio.getAllowedUsers().split(",")).build())
                .build();
    }
}
