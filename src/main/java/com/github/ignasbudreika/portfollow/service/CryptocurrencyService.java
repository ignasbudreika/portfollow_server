package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CryptocurrencyDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.CryptocurrencyInvestmentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentStatsDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.TransactionDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import com.github.ignasbudreika.portfollow.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;

@Slf4j
@Service
public class CryptocurrencyService {
    @Autowired
    private AssetService assetService;

    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private PortfolioHistoryService portfolioHistoryService;

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
        LocalDate date = LocalDate.now();

        return cryptoInvestments.stream().map(investment -> {
            BigDecimal price = assetService.getRecentPrice(investment.getSymbol(), InvestmentType.CRYPTOCURRENCY);

            return CryptocurrencyInvestmentDTO.builder()
                    .id(investment.getId())
                    .symbol(investment.getSymbol())
                    .quantity(investment.getQuantityAt(date).setScale(4, RoundingMode.HALF_UP))
                    .price(price.setScale(8, RoundingMode.HALF_UP))
                    .value(investment.getQuantityAt(date).multiply(price).setScale(2, RoundingMode.HALF_UP))
                    .transactions(investment.getTransactions().stream()
                            .sorted(Comparator.comparing(InvestmentTransaction::getDate))
                            .map(transaction -> TransactionDTO.builder()
                                    .id(transaction.getId())
                                    .quantity(transaction.getQuantity())
                                    .type(transaction.getType())
                                    .date(transaction.getDate()).build())
                            .toArray(TransactionDTO[]::new)).build();
        }).toList();
    }

    public InvestmentStatsDTO getUserCryptoInvestmentsStats(String userId) {
        Collection<Investment> cryptoInvestments = investmentService.getInvestmentsByUserIdAndType(userId, InvestmentType.CRYPTOCURRENCY);

        BigDecimal totalValue = cryptoInvestments.stream().map(investment ->
                investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice())
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal trend = portfolioHistoryService.calculateTrend(cryptoInvestments);
        BigDecimal totalChange = portfolioHistoryService.calculateTotalChange(cryptoInvestments);

        return InvestmentStatsDTO.builder()
                .totalValue(totalValue)
                .trend(trend)
                .totalChange(totalChange).build();
    }
}
