package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDistributionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.DateValueDTO;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.PortfolioHistoryService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {
    @Autowired
    private UserService userService;
    @Autowired
    private PortfolioHistoryService portfolioHistoryService;

    @GetMapping
    public ResponseEntity<PortfolioDTO> getPortfolio() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(portfolioHistoryService.getUserPortfolio(user));
    }

    @GetMapping("/distribution")
    public ResponseEntity<List<PortfolioDistributionDTO>> getPortfolioDistribution(@RequestParam(value = "type", required = false) String type) {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        if (StringUtils.isBlank(type)) {
            return ResponseEntity.ok(portfolioHistoryService.getUserPortfolioDistribution(user));
        }

        return ResponseEntity.ok(portfolioHistoryService.getUserPortfolioDistributionByType(user, InvestmentType.valueOf(type)));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DateValueDTO>> getPortfolioHistory(@RequestParam(value = "type", defaultValue = "WEEKLY") HistoryType type) {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(portfolioHistoryService.getUserPortfolioHistory(user, type));
    }

    @GetMapping("/profit-loss")
    public ResponseEntity<List<DateValueDTO>> getProfitLossHistory(@RequestParam(value = "type", defaultValue = "WEEKLY") HistoryType type) {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(portfolioHistoryService.getUserProfitLossHistory(user, type));
    }

    @GetMapping("/performance")
    public ResponseEntity<List<DateValueDTO>> getPerformanceHistory(@RequestParam(value = "type", defaultValue = "WEEKLY") HistoryType type) {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(portfolioHistoryService.getUserPerformanceHistory(user, type));
    }

    @GetMapping("/performance/compare")
    public ResponseEntity<List<DateValueDTO>> getPerformanceComparison(@RequestParam(value = "type", defaultValue = "WEEKLY") HistoryType type) {
        return ResponseEntity.ok(portfolioHistoryService.getComparisonPerformanceHistory(type));
    }
}
