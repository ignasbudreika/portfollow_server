package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.StockDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.StockInvestmentDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.StockService;
import com.github.ignasbudreika.portfollow.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<StockInvestmentDTO> createStockInvestment(@RequestBody StockDTO stock) throws BusinessLogicException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(stockService.createStockInvestment(stock, user));
    }
}
