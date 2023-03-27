package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CryptocurrencyDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.CryptocurrencyInvestmentDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CryptocurrencyService {
    @Autowired
    private InvestmentService investmentService;

    public CryptocurrencyInvestmentDTO createCryptocurrencyInvestment(CryptocurrencyDTO crypto, User user) {
        Investment investment = Investment.builder()
                .symbol(crypto.getSymbol())
                .quantity(crypto.getQuantity())
                .type(InvestmentType.CRYPTOCURRENCY).build();

        investment = investmentService.createInvestment(investment, user);

        CryptocurrencyInvestmentDTO cryptoInvestment = CryptocurrencyInvestmentDTO.builder()
                .id(investment.getId())
                .symbol(investment.getSymbol()).build();

        return cryptoInvestment;
    }
}
