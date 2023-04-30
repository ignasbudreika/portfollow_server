package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.DateValueDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDistributionDTO;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.Asset;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.PortfolioHistory;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StatisticsService {
    private AssetService assetService;
    private PortfolioHistoryRepository portfolioHistoryRepository;

    public BigDecimal getAssetDayTrend(Asset asset) {
        BigDecimal yesterdaysPrice = assetService.getLatestAssetPriceForDate(asset, LocalDate.now().minusDays(1));
        if (yesterdaysPrice == null || yesterdaysPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return asset.getPrice()
                .subtract(yesterdaysPrice)
                .divide(yesterdaysPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getInvestmentTotalChange(Investment investment) {
        BigDecimal purchasePrice = investment.getTransactions().stream().filter(transaction -> transaction.getType().equals(InvestmentTransactionType.BUY))
                .map(tx -> tx.getQuantity()
                        .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                        .setScale(8, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sellPrice = investment.getTransactions().stream().filter(transaction -> transaction.getType().equals(InvestmentTransactionType.SELL))
                .map(tx -> tx.getQuantity()
                        .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                        .setScale(8, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal daysValue = investment.getQuantity()
                .multiply(investment.getAsset().getPrice())
                .setScale(8, RoundingMode.HALF_UP);

        return daysValue.add(sellPrice).subtract(purchasePrice);
    }

    public BigDecimal calculateTotalChange(Collection<Investment> investments) {
        return investments.stream().map(investment -> {
            BigDecimal purchasePrice = investment.getTransactions().stream().filter(tx -> tx.getType().equals(InvestmentTransactionType.BUY))
                    .map(tx -> tx.getQuantity()
                            .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                            .setScale(8, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sellPrice = investment.getTransactions().stream().filter(tx -> tx.getType().equals(InvestmentTransactionType.SELL))
                    .map(tx -> tx.getQuantity()
                            .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                            .setScale(8, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal currentValue = investment.getQuantityAt(LocalDate.now())
                    .multiply(investment.getAsset().getPrice()).setScale(8, RoundingMode.HALF_UP);

            return currentValue.add(sellPrice).subtract(purchasePrice);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTrend(Collection<Investment> investments) {
        LocalDate now = LocalDate.now();
        LocalDate yesterday = now.minusDays(1);

        BigDecimal currentValue = investments.stream().map(investment ->
                investment.getQuantityAt(now).multiply(investment.getAsset().getPrice()).setScale(8, RoundingMode.HALF_UP)
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal yesterdaysValue = investments.stream().map(investment ->
                investment.getQuantityAt(now).multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), yesterday)).setScale(8, RoundingMode.HALF_UP)
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (yesterdaysValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return currentValue.subtract(yesterdaysValue)
                .divide(yesterdaysValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100L).setScale(2, RoundingMode.HALF_UP));
    }

    public BigDecimal calculateTotalPerformance(Collection<Investment> investments) {
        BigDecimal totalPurchasePrice = investments.stream().map(investment ->
            investment.getTransactions().stream().filter(tx ->
                            tx.getType().equals(InvestmentTransactionType.BUY))
                    .map(tx -> tx.getQuantity()
                            .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                            .setScale(8, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSellPrice = investments.stream().map(investment ->
            investment.getTransactions().stream().filter(tx ->
                            tx.getType().equals(InvestmentTransactionType.SELL))
                    .map(tx -> tx.getQuantity()
                            .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                            .setScale(8, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDaysValue = investments.stream().map(investment ->
            investment.getQuantityAt(LocalDate.now())
                    .multiply(investment.getAsset().getPrice())
                    .setScale(8, RoundingMode.HALF_UP)
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPurchasePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalDaysValue.add(totalSellPrice).subtract(totalPurchasePrice)
                .divide(totalPurchasePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100L).setScale(2, RoundingMode.HALF_UP));
    }

    public List<DateValueDTO> getComparisonPerformanceHistory(HistoryType type) {
        LocalDate from = LocalDate.now();
        switch (type) {
            case WEEKLY -> from = from.minusWeeks(1L);
            case MONTHLY -> from = from.minusMonths(1L);
            case QUARTERLY -> from = from.minusMonths(3L);
            case ALL -> from = LocalDate.of(2023, 1, 1);
        }

        Asset asset = assetService.getAsset("SPY", InvestmentType.STOCK);
        BigDecimal initialValue = assetService.getLatestAssetPriceForDate(asset, from);
        List<DateValueDTO> history = new ArrayList<>();

        for (; !from.isAfter(LocalDate.now()); from = from.plusDays(1)) {
            BigDecimal daysValue = assetService.getLatestAssetPriceForDate(asset, from);

            BigDecimal change = daysValue.subtract(initialValue)
                    .divide(initialValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100L).setScale(2, RoundingMode.HALF_UP));

            history.add(DateValueDTO.builder().date(from).value(change).build());
        }

        return history;
    }

    public List<PortfolioDistributionDTO> getUserPortfolioDistribution(User user) {
        PortfolioHistory history = portfolioHistoryRepository.findFirstByUserIdOrderByDateDesc(user.getId());
        if (history == null) {
            return new ArrayList<>();
        }

        BigDecimal portfolioValue = history.getInvestments().stream().map(investment ->
                investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice())
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (portfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        Map<InvestmentType, List<Investment>> investmentsByType = history.getInvestments().stream().collect(
                Collectors.groupingBy(Investment::getType));

        List<PortfolioDistributionDTO> distribution = investmentsByType.entrySet().stream().map(typeInvestments -> {
            BigDecimal value = typeInvestments.getValue().stream().map(investment ->
                    investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice())
            ).reduce(BigDecimal.ZERO, BigDecimal::add);

            return PortfolioDistributionDTO.builder()
                    .label(typeInvestments.getKey().toString())
                    .value(value)
                    .percentage(value
                            .divide(portfolioValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                    .build();
        }).toList();

        return distribution;
    }

    public List<PortfolioDistributionDTO> getUserPortfolioDistributionByType(User user, InvestmentType type) {
        PortfolioHistory history = portfolioHistoryRepository.findFirstByUserIdOrderByDateDesc(user.getId());
        if (history == null) {
            return new ArrayList<>();
        }

        Collection<Investment> investments = history.getInvestments().stream().filter(investment -> investment.getType().equals(type)).toList();

        BigDecimal totalValue = investments.stream().map(investment ->
                investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice())
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PortfolioDistributionDTO> distribution = investments.stream().map(investment ->
                PortfolioDistributionDTO.builder()
                        .label(investment.getSymbol())
                        .value(investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice()))
                        .percentage(investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice())
                                .divide(totalValue, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                        .build()
        ).toList();

        return distribution;
    }

}
