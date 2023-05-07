package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.StockDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.StockInvestmentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentStatsDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.StockService;
import com.github.ignasbudreika.portfollow.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/investment/stock")
public class StockController {
    @Autowired
    private StockService stockService;
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Collection<StockInvestmentDTO>> getUserStockInvestments() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(stockService.getUserStockInvestments(user.getId()));
    }

    @GetMapping("/stats")
    public ResponseEntity<InvestmentStatsDTO> getUserStockInvestmentsStats() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(stockService.getUserStockInvestmentsStats(user.getId()));
    }

    @PostMapping
    public ResponseEntity<StockInvestmentDTO> createStockInvestment(@Valid @RequestBody StockDTO stock) throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(stockService.createStockInvestment(stock, user));
    }
}
