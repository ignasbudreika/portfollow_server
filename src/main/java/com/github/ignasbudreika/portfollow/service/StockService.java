package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.StockDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.StockInvestmentDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.external.client.AlphaVantageClient;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;

@Slf4j
@Service
public class StockService {
    @Autowired
    private AlphaVantageClient alphaVantageClient;

    @Autowired
    private InvestmentService investmentService;

    public Collection<StockInvestmentDTO> getUserStockInvestments(String userId) {
        Collection<Investment> stockInvestments = investmentService.getInvestmentsByUserIdAndType(userId, InvestmentType.STOCK);

        Collection<StockInvestmentDTO> result = stockInvestments.stream().map(investment -> {
            com.github.ignasbudreika.portfollow.external.dto.response.StockDTO stock = new com.github.ignasbudreika.portfollow.external.dto.response.StockDTO();
            try {
                stock = alphaVantageClient.getStockData(investment.getSymbol());
            } catch (Exception e) {
            }

            return StockInvestmentDTO.builder()
                    .id(investment.getId())
                    .ticker(investment.getSymbol())
                    .value(stock.getPrice() == null ?
                            null : investment.getQuantity().multiply(new BigDecimal(stock.getPrice())).setScale(2)).build();
        }).toList();

        return result;
    }

    public StockInvestmentDTO createStockInvestment(StockDTO stock, User user) {
        Investment investment = Investment.builder()
                .symbol(stock.getTicker())
                .quantity(stock.getQuantity())
                .type(InvestmentType.STOCK).build();

        investment = investmentService.createInvestment(investment, user);

        StockInvestmentDTO stockInvestment = StockInvestmentDTO.builder()
                .id(investment.getId())
                .ticker(investment.getSymbol()).build();

        return stockInvestment;
    }
}
