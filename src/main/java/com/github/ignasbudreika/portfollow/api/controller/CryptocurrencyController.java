package com.github.ignasbudreika.portfollow.api.controller;

import com.github.ignasbudreika.portfollow.api.dto.request.CryptocurrencyDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.CryptocurrencyInvestmentDTO;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.service.CryptocurrencyService;
import com.github.ignasbudreika.portfollow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/investment/crypto")
public class CryptocurrencyController {
    @Autowired
    private UserService userService;
    @Autowired
    private CryptocurrencyService cryptocurrencyService;

    @PostMapping
    public CryptocurrencyInvestmentDTO createCryptocurrencyInvestment(@RequestBody CryptocurrencyDTO cryptocurrencyDTO) throws BusinessLogicException {
        User user = userService.getByGoogleId(SecurityContextHolder.getContext().getAuthentication().getName());

        return cryptocurrencyService.createCryptocurrencyInvestment(cryptocurrencyDTO, user);
    }
}
