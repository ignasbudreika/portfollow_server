package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDistributionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.DateValueDTO;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.*;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.PortfolioHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortfolioHistoryService {
    @Autowired
    private AssetService assetService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private PortfolioHistoryRepository portfolioHistoryRepository;
    @Autowired
    private InvestmentRepository investmentRepository;

    @Transactional
    public PortfolioHistory saveCurrentPortfolio(String userId) {
        LocalDate date = LocalDate.now();
        PortfolioHistory portfolioHistory = portfolioHistoryRepository.findFirstByUserIdAndDate(userId, date);
        if (portfolioHistory == null) {
            PortfolioHistory lastDaysPortfolioHistory = portfolioHistoryRepository.findFirstByUserIdAndDateBeforeOrderByDateDesc(userId, date);
            if (lastDaysPortfolioHistory == null) {
                return portfolioHistoryRepository.save(PortfolioHistory.builder()
                        .user(User.builder().id(userId).build())
                        .value(BigDecimal.ZERO)
                        .investments(new ArrayList<>())
                        .date(date).build());
            }

            Collection<Investment> investments = lastDaysPortfolioHistory.getInvestments().stream().collect(Collectors.toList());
            BigDecimal totalValue = investments.stream().map(investment ->
                    investment.getQuantityAt(date).multiply(investment.getAsset().getPrice()).setScale(8, RoundingMode.HALF_UP)
            ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

            return portfolioHistoryRepository.save(PortfolioHistory.builder()
                    .user(User.builder().id(userId).build())
                    .value(totalValue)
                    .investments(investments)
                    .date(date).build());
        }

        BigDecimal totalValue = portfolioHistory.getInvestments().stream().map(investment ->
                investment.getQuantityAt(date).multiply(investment.getAsset().getPrice()).setScale(8, RoundingMode.HALF_UP)
        ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        portfolioHistory.setValue(totalValue);
        return portfolioHistoryRepository.save(portfolioHistory);
    }

    public void initPortfolio(Portfolio portfolio) {
        for (LocalDate date = LocalDate.now(); date.isAfter(LocalDate.now().minusDays(7)); date = date.minusDays(1)) {
            portfolioHistoryRepository.save(PortfolioHistory.builder()
                    .date(date)
                    .user(portfolio.getUser())
                    .value(BigDecimal.ZERO).build());
        }
    }

    public PortfolioDTO getUserPortfolio(User user) {
        PortfolioHistory portfolioHistory = portfolioHistoryRepository.findFirstByUserIdOrderByDateDesc(user.getId());
        BigDecimal totalValue = portfolioHistory == null ?
                BigDecimal.ZERO :
                portfolioHistory.getInvestments().stream().map(investment ->
                    investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice())
                ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        boolean isEmpty = !investmentRepository.existsByUserId(user.getId());

        if (portfolioHistory == null) {
            return PortfolioDTO.builder()
                    .isEmpty(isEmpty)
                    .totalValue(totalValue)
                    .totalChange(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .trend(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)).build();
        }

        Collection<Investment> investments = investmentRepository.findAllByUserId(user.getId());

        return PortfolioDTO.builder()
                .isEmpty(isEmpty)
                .totalValue(totalValue)
                .totalChange(statisticsService.calculateTotalChange(investments))
                .trend(statisticsService.calculateTrend(investments))
                .build();
    }

    public List<DateValueDTO> getUserProfitLossHistory(User user, HistoryType type) {
        LocalDate from = LocalDate.now();
        switch (type) {
            case WEEKLY -> from = from.minusWeeks(1L);
            case MONTHLY -> from = from.minusMonths(1L);
            case QUARTERLY -> from = from.minusMonths(3L);
            case ALL -> from = LocalDate.of(2023, 1, 1);
        }

        Collection<Investment> investments = investmentRepository.findAllByUserId(user.getId());
        BigDecimal initialProfitLoss = null;

        List<DateValueDTO> history = new ArrayList<>();
        for (LocalDate at = from; !at.isAfter(LocalDate.now()); at = at.plusDays(1)) {
            LocalDate finalAt = at;

            BigDecimal daysProfitLoss = investments.stream().map(investment -> {
                BigDecimal purchasePrice = investment.getTransactions().stream().filter(tx ->
                                tx.getType().equals(InvestmentTransactionType.BUY) && !tx.getDate().isAfter(finalAt))
                        .map(tx -> tx.getQuantity()
                                .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                                .setScale(8, RoundingMode.HALF_UP))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal sellPrice = investment.getTransactions().stream().filter(tx ->
                                tx.getType().equals(InvestmentTransactionType.SELL) && !tx.getDate().isAfter(finalAt))
                        .map(tx -> tx.getQuantity()
                                .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                                .setScale(8, RoundingMode.HALF_UP))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal daysValue = investment.getQuantityAt(finalAt)
                        .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), finalAt))
                        .setScale(8, RoundingMode.HALF_UP);

                return daysValue.add(sellPrice).subtract(purchasePrice);
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            if (initialProfitLoss == null) {
                initialProfitLoss = daysProfitLoss;
            }

            daysProfitLoss = daysProfitLoss.subtract(initialProfitLoss);

            history.add(DateValueDTO.builder().date(at).value(daysProfitLoss).build());
        }

        return history;
    }

    public List<DateValueDTO> getUserPerformanceHistory(User user, HistoryType type) {
        LocalDate from = LocalDate.now();
        switch (type) {
            case WEEKLY -> from = from.minusWeeks(1L);
            case MONTHLY -> from = from.minusMonths(1L);
            case QUARTERLY -> from = from.minusMonths(3L);
            case ALL -> from = LocalDate.of(2023, 1, 1);
        }

        Collection<Investment> investments = investmentRepository.findAllByUserId(user.getId());

        List<DateValueDTO> history = new ArrayList<>();
        BigDecimal initialPerformance = null;
        for (LocalDate at = from; !at.isAfter(LocalDate.now()); at = at.plusDays(1)) {
            LocalDate finalAt = at;

            BigDecimal totalPurchasePrice = investments.stream().map(investment ->
                        investment.getTransactions().stream().filter(tx ->
                                        tx.getType().equals(InvestmentTransactionType.BUY) && !tx.getDate().isAfter(finalAt))
                                .map(tx -> tx.getQuantity()
                                        .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                                        .setScale(8, RoundingMode.HALF_UP))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                    ).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalSellPrice = investments.stream().map(investment ->
                investment.getTransactions().stream().filter(tx ->
                                tx.getType().equals(InvestmentTransactionType.SELL) && !tx.getDate().isAfter(finalAt))
                        .map(tx -> tx.getQuantity()
                                .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), tx.getDate()))
                                .setScale(8, RoundingMode.HALF_UP))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
            ).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDaysValue = investments.stream().map(investment ->
                investment.getQuantityAt(finalAt)
                        .multiply(assetService.getLatestAssetPriceForDate(investment.getAsset(), finalAt))
                        .setScale(8, RoundingMode.HALF_UP)
            ).reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalPurchasePrice.compareTo(BigDecimal.ZERO) == 0) {
                if (initialPerformance == null) {
                    initialPerformance = BigDecimal.ZERO;
                }

                history.add(DateValueDTO.builder().date(at).value(BigDecimal.ZERO.subtract(initialPerformance)).build());
            } else {
                BigDecimal performance = totalDaysValue.add(totalSellPrice).subtract(totalPurchasePrice)
                        .divide(totalPurchasePrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100L).setScale(2, RoundingMode.HALF_UP));

                if (initialPerformance == null) {
                    initialPerformance = performance;
                }

                performance = performance.subtract(initialPerformance);

                history.add(DateValueDTO.builder().date(at).value(performance).build());
            }
        }

        return history;
    }

    public List<DateValueDTO> getUserPortfolioHistory(User user, HistoryType type) {
        LocalDate from = LocalDate.now();
        switch (type) {
            case WEEKLY -> from = from.minusWeeks(1L);
            case MONTHLY -> from = from.minusMonths(1L);
            case QUARTERLY -> from = from.minusMonths(3L);
            case ALL -> from = LocalDate.of(2022, 12, 31);
        }

        Collection<PortfolioHistory> portfolioHistories = portfolioHistoryRepository.findAllByUserIdAndDateAfterOrderByDateAsc(user.getId(), from);

        return portfolioHistories.stream().map(portfolioHistory -> DateValueDTO.builder()
                .value(portfolioHistory.getValue().setScale(2, RoundingMode.HALF_UP))
                .date(portfolioHistory.getDate()).build())
                .toList();
    }

    public void createOrUpdatePortfolioHistory(Investment investment) {
        for (LocalDate date = investment.getDate(); date.isBefore(LocalDate.now().plusDays(1)); date = date.plusDays(1))
        {
            final LocalDate day = date;

            PortfolioHistory portfolioHistory = portfolioHistoryRepository.findFirstByUserIdAndDate(investment.getUser().getId(), date);
            if (portfolioHistory != null) {
                Collection<Investment> investments = portfolioHistory.getInvestments()
                        .stream().filter(i -> !i.getId().equals(investment.getId())).collect(Collectors.toList());
                investments.add(investment);
                portfolioHistory.setInvestments(investments);

                BigDecimal totalValue = investments.stream().map(previousDayInvestment ->
                        previousDayInvestment.getQuantityAt(day).multiply(
                                assetService.getLatestAssetPriceForDate(previousDayInvestment.getAsset(), day)
                        ).setScale(8, RoundingMode.HALF_UP)
                ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(8, RoundingMode.HALF_UP);

                portfolioHistory.setValue(totalValue);
            } else {
                PortfolioHistory lastDaysPortfolioHistory = portfolioHistoryRepository.findFirstByUserIdAndDate(investment.getUser().getId(), date.minusDays(1));
                if (lastDaysPortfolioHistory != null) {
                    Collection<Investment> investments = lastDaysPortfolioHistory.getInvestments()
                            .stream().filter(i -> !i.getId().equals(investment.getId())).collect(Collectors.toList());
                    investments.add(investment);

                    BigDecimal totalValue = investments.stream().map(previousDayInvestment ->
                            previousDayInvestment.getQuantityAt(day).multiply(
                                    assetService.getLatestAssetPriceForDate(previousDayInvestment.getAsset(), day)
                            ).setScale(8, RoundingMode.HALF_UP)
                    ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP).setScale(8, RoundingMode.HALF_UP);

                    portfolioHistory = PortfolioHistory.builder()
                            .value(totalValue)
                            .date(date)
                            .investments(investments)
                            .user(investment.getUser()).build();
                } else {
                    portfolioHistory = PortfolioHistory.builder()
                            .value(investment.getQuantityAt(day).multiply(
                                    assetService.getLatestAssetPriceForDate(investment.getAsset(), date)
                            ).setScale(8, RoundingMode.HALF_UP))
                            .investments(Set.of(investment))
                            .date(date)
                            .user(investment.getUser()).build();
                }
            }

            log.info("saving portfolio for user: {} at: {}, total value: {}",
                    investment.getUser().getId(), date, portfolioHistory.getValue());

            portfolioHistoryRepository.save(portfolioHistory);
        }
    }

    public void updatePortfolioHistoryValue(User user, LocalDate from) {
        for (; from.isBefore(LocalDate.now().plusDays(1)); from = from.plusDays(1)) {
            final LocalDate day = from;

            PortfolioHistory portfolioHistory = portfolioHistoryRepository.findFirstByUserIdAndDate(user.getId(), from);
            if (portfolioHistory != null) {
                BigDecimal totalValue = portfolioHistory.getInvestments().stream().map(previousDayInvestment ->
                        previousDayInvestment.getQuantityAt(day).multiply(
                                assetService.getLatestAssetPriceForDate(previousDayInvestment.getAsset(), day)
                        ).setScale(8, RoundingMode.HALF_UP)
                ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP).setScale(8, RoundingMode.HALF_UP);

                portfolioHistory.setValue(totalValue);

                portfolioHistoryRepository.save(portfolioHistory);
            }
        }
    }
}
