package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.PortfolioService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {
    @Autowired
    private UserService userService;
    @Autowired
    private PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<PortfolioDTO> getPortfolioDistribution(@RequestParam(value = "type", required = false) String type) {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        if (StringUtils.isBlank(type)) {
            return ResponseEntity.ok(portfolioService.getUserPortfolio(user));
        }

        return ResponseEntity.ok(portfolioService.getUserPortfolioByType(user, InvestmentType.valueOf(type)));
    }
}
