package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.external.client.AlphaVantageClient;
import com.github.ignasbudreika.portfollow.external.dto.StockDTO;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Collection;

@Service
public class InvestmentService {
    @Autowired
    private AlphaVantageClient alphaVantageClient;

    @Autowired
    private InvestmentRepository investmentRepository;

    public Collection<InvestmentDTO> getInvestmentsByUserId(String userId) {
        Collection<Investment> investments = investmentRepository.findAllByUserId(userId);

        Collection<InvestmentDTO> result = investments.stream().map(investment -> {
            switch (investment.getType()) {
                case STOCK -> {
                    StockDTO stock = new StockDTO();
                    try {
                        stock = alphaVantageClient.getStockData(investment.getSymbol());
                    } catch (Exception e) {
                    }

                    return InvestmentDTO.builder()
                            .id(investment.getId())
                            .type(InvestmentType.STOCK)
                            .symbol(investment.getSymbol())
                            .value(stock.getPrice() == null ?
                                    null : investment.getQuantity().multiply(new BigDecimal(stock.getPrice())).setScale(2)).build();
                }
            }

            return InvestmentDTO.builder()
                    .id(investment.getId())
                    .symbol(investment.getSymbol()).build();
        }).toList();

        return result;
    }

    public Collection<Investment> getInvestmentsByUserIdAndType(String userId, InvestmentType type) {
        return investmentRepository.findAllByUserIdAndType(userId, type);
    }

    public Investment getInvestmentById(String id) {
        return investmentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public Investment createInvestment(Investment investment, User user) {
        investment.setUser(user);

        return investmentRepository.save(investment);
    }
}
