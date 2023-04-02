package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CryptocurrencyDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.CryptocurrencyInvestmentDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

@Slf4j
@Service
public class CryptocurrencyService {
    @Autowired
    private AssetService assetService;

    @Autowired
    private InvestmentService investmentService;

    public CryptocurrencyInvestmentDTO createCryptocurrencyInvestment(CryptocurrencyDTO crypto, User user) throws BusinessLogicException {
        Investment investment = Investment.builder()
                .symbol(crypto.getSymbol())
                .quantity(crypto.getQuantity())
                .type(InvestmentType.CRYPTOCURRENCY)
                .date(crypto.getDate()).build();

        investment = investmentService.createInvestment(investment, user);

        CryptocurrencyInvestmentDTO cryptoInvestment = CryptocurrencyInvestmentDTO.builder()
                .id(investment.getId())
                .symbol(investment.getSymbol()).build();

        return cryptoInvestment;
    }

    public Collection<CryptocurrencyInvestmentDTO> getUserCryptocurrencyInvestments(String userId) {
        Collection<Investment> cryptoInvestments = investmentService.getInvestmentsByUserIdAndType(userId, InvestmentType.CRYPTOCURRENCY);

        return cryptoInvestments.stream().map(investment -> {
            BigDecimal price = assetService.getRecentPrice(investment.getSymbol(), InvestmentType.CRYPTOCURRENCY);

            return CryptocurrencyInvestmentDTO.builder()
                    .id(investment.getId())
                    .symbol(investment.getSymbol())
                    .quantity(investment.getQuantity().setScale(4, RoundingMode.HALF_UP))
                    .price(price.setScale(8, RoundingMode.HALF_UP))
                    .value(investment.getQuantity().multiply(price).setScale(2, RoundingMode.HALF_UP))
                    .date(investment.getDate()).build();
        }).toList();
    }
}
