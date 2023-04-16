package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.CurrencyDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.CurrencyInvestmentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentStatsDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.CurrencyService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/investment/currency")
public class CurrencyController {
    @Autowired
    private UserService userService;
    @Autowired
    private CurrencyService currencyService;

    @PostMapping
    public CurrencyInvestmentDTO createCurrencyInvestment(@RequestBody CurrencyDTO currencyDTO) throws BusinessLogicException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return currencyService.createCurrencyInvestment(currencyDTO, user);
    }

    @GetMapping
    public ResponseEntity<Collection<CurrencyInvestmentDTO>> getUserCryptocurrencyInvestments() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(currencyService.getUserCurrencyInvestments(user.getId()));
    }

    @GetMapping("/stats")
    public ResponseEntity<InvestmentStatsDTO> getUserCryptoInvestmentsStats() {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(currencyService.getUserCryptoInvestmentsStats(user.getId()));
    }
}
