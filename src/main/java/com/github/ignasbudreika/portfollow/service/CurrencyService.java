package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CurrencyDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.CurrencyInvestmentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentStatsDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.TransactionDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import com.github.ignasbudreika.portfollow.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;

@Slf4j
@Service
@AllArgsConstructor
public class CurrencyService {
    private StatisticsService statisticsService;
    private InvestmentService investmentService;
    private PortfolioHistoryService portfolioHistoryService;

    public CurrencyInvestmentDTO createCurrencyInvestment(CurrencyDTO currency, User user) throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        Investment investment = Investment.builder()
                .symbol(currency.getSymbol())
                .quantity(currency.getQuantity())
                .amount(currency.getAmount())
                .type(currency.isCrypto() ? InvestmentType.CRYPTO : InvestmentType.FIAT)
                .updateType(InvestmentUpdateType.getUpdateType(currency.getPeriod()))
                .date(currency.getDate()).build();

        investment = investmentService.createInvestment(investment, user);

        return CurrencyInvestmentDTO.builder()
                .id(investment.getId())
                .symbol(investment.getSymbol()).build();
    }

    public Collection<CurrencyInvestmentDTO> getUserCurrencyInvestments(String userId) {
        Collection<Investment> investments = investmentService.getInvestmentsByUserIdAndType(userId, InvestmentType.CRYPTO);
        Collection<Investment> forexInvestments = investmentService.getInvestmentsByUserIdAndType(userId, InvestmentType.FIAT);
        investments.addAll(forexInvestments);
        LocalDate date = LocalDate.now();

        return investments.stream().map(investment ->
            CurrencyInvestmentDTO.builder()
                    .id(investment.getId())
                    .symbol(investment.getSymbol())
                    .quantity(investment.getQuantityAt(date).setScale(8, RoundingMode.HALF_UP))
                    .price(investment.getAsset().getPrice().setScale(8, RoundingMode.HALF_UP))
                    .value(investment.getQuantityAt(date).multiply(investment.getAsset().getPrice()).setScale(2, RoundingMode.HALF_UP))
                    .crypto(investment.getType().equals(InvestmentType.CRYPTO))
                    .dayTrend(statisticsService.getAssetDayTrend(investment.getAsset()))
                    .totalChange(statisticsService.getInvestmentTotalChange(investment))
                    .updateType(investment.getUpdateType().toString())
                    .transactions(investment.getTransactions().stream()
                            .sorted(Comparator.comparing(InvestmentTransaction::getDate))
                            .map(transaction -> TransactionDTO.builder()
                                    .id(transaction.getId())
                                    .quantity(transaction.getQuantity())
                                    .type(transaction.getType())
                                    .date(transaction.getDate()).build())
                            .toArray(TransactionDTO[]::new)).build()
        ).toList();
    }

    public InvestmentStatsDTO getUserCurrencyInvestmentsStats(String userId) {
        Collection<Investment> investments = investmentService.getInvestmentsByUserIdAndType(userId, InvestmentType.CRYPTO);
        Collection<Investment> forexInvestments = investmentService.getInvestmentsByUserIdAndType(userId, InvestmentType.FIAT);
        investments.addAll(forexInvestments);

        BigDecimal totalValue = investments.stream().map(investment ->
                investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice())
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal trend = statisticsService.calculateTrend(investments);
        BigDecimal totalChange = statisticsService.calculateTotalChange(investments);

        return InvestmentStatsDTO.builder()
                .totalValue(totalValue)
                .trend(trend)
                .totalChange(totalChange).build();
    }
}
