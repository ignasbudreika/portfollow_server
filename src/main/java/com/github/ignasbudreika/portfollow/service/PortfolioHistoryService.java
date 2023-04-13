package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDistributionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioHistoryDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.ProfitLossDTO;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.PortfolioHistory;
import com.github.ignasbudreika.portfollow.model.User;
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
                    investment.getQuantityAt(date).multiply(
                            assetService.getRecentPrice(investment.getSymbol(), investment.getType())
                    ).setScale(8, RoundingMode.HALF_UP)
            ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

            return portfolioHistoryRepository.save(PortfolioHistory.builder()
                    .user(User.builder().id(userId).build())
                    .value(totalValue)
                    .investments(investments)
                    .date(date).build());
        }

        BigDecimal totalValue = portfolioHistory.getInvestments().stream().map(investment ->
                investment.getQuantityAt(date).multiply(
                        assetService.getRecentPrice(investment.getSymbol(), investment.getType())
                ).setScale(8, RoundingMode.HALF_UP)
        ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        portfolioHistory.setValue(totalValue);
        return portfolioHistoryRepository.save(portfolioHistory);
    }

    public void initPortfolio(Portfolio portfolio) {
        for (LocalDate date = LocalDate.now(); date.isAfter(LocalDate.now().minusDays(7)); date = date.minusDays(1)) {
            portfolioHistoryRepository.save(PortfolioHistory.builder()
                    .date(date)
                    .portfolio(portfolio)
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
                ).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean isEmpty = !investmentRepository.existsByUserId(user.getId());

        PortfolioHistory lastDaysPortfolioHistory = portfolioHistoryRepository.findFirstByUserIdAndDateBeforeOrderByDateDesc(user.getId(), LocalDateTime.now().toLocalDate());

        if (portfolioHistory == null || lastDaysPortfolioHistory == null || lastDaysPortfolioHistory.getValue().compareTo(BigDecimal.ZERO) == 0) {
            return PortfolioDTO.builder()
                    .isEmpty(isEmpty)
                    .totalValue(totalValue)
                    .totalChange(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .trend(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)).build();
        }

        return PortfolioDTO.builder()
                .isEmpty(isEmpty)
                .totalValue(totalValue)
                .totalChange(calculateTotalChange(user))
                .trend(calculateTrend(portfolioHistory.getInvestments()))
                .build();
    }

    private BigDecimal calculateTotalChange(User user) {
        return investmentRepository.findAllByUserId(user.getId()).stream().map(investment -> {
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

    private BigDecimal calculateTrend(Collection<Investment> investments) {
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

    public List<ProfitLossDTO> getUserProfitLossHistory(User user, HistoryType type) {
        LocalDate from = LocalDate.now();
        switch (type) {
            case WEEKLY -> from = from.minusWeeks(1L);
            case MONTHLY -> from = from.minusMonths(1L);
            case QUARTERLY -> from = from.minusMonths(3L);
            case ALL -> from = LocalDate.of(2023, 1, 1);
        }

        Collection<Investment> investments = investmentRepository.findAllByUserId(user.getId());

        List<ProfitLossDTO> history = new ArrayList<>();
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

            history.add(ProfitLossDTO.builder().date(at).value(daysProfitLoss).build());
        }

        return history;
    }

    public List<PortfolioDistributionDTO> getUserPortfolioDistribution(User user) {
        PortfolioHistory portfolioHistory = portfolioHistoryRepository.findFirstByUserIdOrderByDateDesc(user.getId());
        if (portfolioHistory == null) {
            return new ArrayList<>();
        }

        BigDecimal portfolioValue = portfolioHistory.getInvestments().stream().map(investment ->
                investment.getQuantityAt(LocalDate.now()).multiply(investment.getAsset().getPrice())
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (portfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        Map<InvestmentType, List<Investment>> investmentsByType = portfolioHistory.getInvestments().stream().collect(
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
        PortfolioHistory portfolioHistory = portfolioHistoryRepository.findFirstByUserIdOrderByDateDesc(user.getId());
        if (portfolioHistory == null) {
            return new ArrayList<>();
        }

        Collection<Investment> investments = portfolioHistory.getInvestments().stream().filter(investment -> investment.getType().equals(type)).toList();

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

    public List<PortfolioHistoryDTO> getUserPortfolioHistory(User user, HistoryType type) {
        LocalDate from = LocalDate.now();
        switch (type) {
            case WEEKLY -> from = from.minusWeeks(1L);
            case MONTHLY -> from = from.minusMonths(1L);
            case QUARTERLY -> from = from.minusMonths(3L);
            case ALL -> from = LocalDate.of(2022, 12, 31);
        }

        Collection<PortfolioHistory> portfolioHistories = portfolioHistoryRepository.findAllByUserIdAndDateAfterOrderByDateAsc(user.getId(), from);

        return portfolioHistories.stream().map(portfolioHistory -> PortfolioHistoryDTO.builder()
                .value(portfolioHistory.getValue().setScale(4, RoundingMode.HALF_UP))
                .time(portfolioHistory.getDate()).build())
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
                ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP).setScale(8, RoundingMode.HALF_UP);

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
