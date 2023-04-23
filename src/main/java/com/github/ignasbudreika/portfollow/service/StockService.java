package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.StockDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.StockInvestmentDTO;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;

@Slf4j
@Service
public class StockService {
    @Autowired
    private AssetService assetService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private PortfolioHistoryService portfolioHistoryService;

    public Collection<StockInvestmentDTO> getUserStockInvestments(String userId) {
        Collection<Investment> stockInvestments = investmentService.getInvestmentsByUserIdAndType(userId, InvestmentType.STOCK);
        LocalDate date = LocalDate.now();

        return stockInvestments.stream().map(investment ->
            StockInvestmentDTO.builder()
                    .id(investment.getId())
                    .ticker(investment.getSymbol())
                    .quantity(investment.getQuantityAt(date).setScale(2, RoundingMode.HALF_UP))
                    .price(investment.getAsset().getPrice().setScale(2, RoundingMode.HALF_UP))
                    .value(investment.getQuantityAt(date).multiply(investment.getAsset().getPrice()).setScale(2, RoundingMode.HALF_UP))
                    .dayTrend(statisticsService.getAssetDayTrend(investment.getAsset()))
                    .totalChange(statisticsService.getInvestmentTotalChange(investment))
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

    public InvestmentStatsDTO getUserStockInvestmentsStats(String userId) {
        Collection<Investment> stockInvestments = investmentService.getInvestmentsByUserIdAndType(userId, InvestmentType.STOCK);

        BigDecimal totalValue = stockInvestments.stream().map(investment ->
                investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice())
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal trend = statisticsService.calculateTrend(stockInvestments);
        BigDecimal totalChange = statisticsService.calculateTotalChange(stockInvestments);

        return InvestmentStatsDTO.builder()
                .totalValue(totalValue)
                .trend(trend)
                .totalChange(totalChange).build();
    }

    public StockInvestmentDTO createStockInvestment(StockDTO stock, User user) throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        Investment investment = Investment.builder()
                .symbol(stock.getTicker())
                .quantity(stock.getQuantity())
                .type(InvestmentType.STOCK)
                .date(stock.getDate()).build();

        investment = investmentService.createInvestment(investment, user);

        return StockInvestmentDTO.builder()
                .id(investment.getId())
                .ticker(investment.getSymbol()).build();
    }
}
