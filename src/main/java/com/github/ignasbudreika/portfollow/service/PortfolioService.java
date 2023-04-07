package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDistributionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioHistoryDTO;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
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
public class PortfolioService {
    @Autowired
    private AssetService assetService;
    @Autowired
    private InvestmentRepository investmentRepository;
    @Autowired
    private PortfolioRepository portfolioRepository;

    @Transactional
    public Portfolio saveCurrentPortfolio(String userId) {
        LocalDate date = LocalDate.now();
        Portfolio portfolio = portfolioRepository.findFirstByUserIdAndDate(userId, date);
        if (portfolio == null) {
            Collection<Investment> investments = investmentRepository.findAllByUserId(userId);
            BigDecimal totalValue = investments.stream().map(investment ->
                    investment.getQuantity().multiply(
                            assetService.getRecentPrice(investment.getSymbol(), investment.getType())
                    ).setScale(8, RoundingMode.HALF_UP)
            ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

            return portfolioRepository.save(Portfolio.builder()
                    .user(User.builder().id(userId).build())
                    .value(totalValue)
                    .investments(investments)
                    .date(date).build());
        }

        Collection<Investment> investments = investmentRepository.findByPortfoliosId(portfolio.getId());
        BigDecimal totalValue = investments.stream().map(investment ->
                investment.getQuantity().multiply(
                        assetService.getRecentPrice(investment.getSymbol(), investment.getType())
                ).setScale(8, RoundingMode.HALF_UP)
        ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        portfolio.setValue(totalValue);
        portfolio.setInvestments(investments);
        return portfolioRepository.save(portfolio);
    }

    // todo fix so that it shows same value as in distribution
    public PortfolioDTO getUserPortfolio(User user) {
        Portfolio portfolio = portfolioRepository.findFirstByUserIdAndDateBeforeOrderByDateDesc(user.getId(), LocalDate.now().plusDays(1));
        BigDecimal totalValue = portfolio == null ? BigDecimal.ZERO : portfolio.getValue();

        Portfolio lastDaysPortfolio = portfolioRepository.findFirstByUserIdAndDateBeforeOrderByDateDesc(user.getId(), LocalDateTime.now().toLocalDate());

        if (lastDaysPortfolio == null || lastDaysPortfolio.getValue().equals(BigDecimal.ZERO)) {
            return PortfolioDTO.builder().totalValue(totalValue).change(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)).build();
        }

        return PortfolioDTO.builder()
                .totalValue(totalValue)
                .change(totalValue.subtract(lastDaysPortfolio.getValue())
                        .divide(lastDaysPortfolio.getValue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100L).setScale(2, RoundingMode.HALF_UP)))
                .build();
    }

    public List<PortfolioDistributionDTO> getUserPortfolioDistribution(User user) {
        Portfolio portfolio = portfolioRepository.findFirstByUserIdOrderByDateDesc(user.getId());
        if (portfolio == null) {
            return new ArrayList<>();
        }

        Collection<Investment> investments = investmentRepository.findByPortfoliosId(portfolio.getId());

        Map<InvestmentType, List<Investment>> investmentsByType = investments.stream().collect(
                Collectors.groupingBy(Investment::getType));

        List<PortfolioDistributionDTO> distribution = investmentsByType.entrySet().stream().map(typeInvestments -> {
            BigDecimal value = typeInvestments.getValue().stream().map(investment ->
                    investment.getQuantity().multiply(investment.getAsset().getPrice())
            ).reduce(BigDecimal.ZERO, BigDecimal::add);

            return PortfolioDistributionDTO.builder()
                    .label(typeInvestments.getKey().toString())
                    .value(value)
                    .percentage(value
                            .divide(portfolio.getValue(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                    .build();
        }).toList();

        return distribution;
    }

    public List<PortfolioDistributionDTO> getUserPortfolioDistributionByType(User user, InvestmentType type) {
        Portfolio portfolio = portfolioRepository.findFirstByUserIdOrderByDateDesc(user.getId());
        if (portfolio == null) {
            return new ArrayList<>();
        }

        Collection<Investment> investments = investmentRepository.findByTypeAndPortfoliosId(type, portfolio.getId());

        BigDecimal totalValue = investments.stream().map(investment ->
                investment.getQuantity().multiply(investment.getAsset().getPrice())
        ).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PortfolioDistributionDTO> distribution = investments.stream().map(investment ->
                PortfolioDistributionDTO.builder()
                    .label(investment.getSymbol())
                    .value(investment.getQuantity().multiply(investment.getAsset().getPrice()))
                    .percentage(investment.getQuantity().multiply(investment.getAsset().getPrice())
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

        Collection<Portfolio> portfolios = portfolioRepository.findAllByUserIdAndDateAfterOrderByDateAsc(user.getId(), from);

        return portfolios.stream().map(portfolio -> PortfolioHistoryDTO.builder()
                .value(portfolio.getValue().setScale(4, RoundingMode.HALF_UP))
                .time(portfolio.getDate()).build())
                .toList();
    }

    public void createOrUpdatePortfolioHistory(Investment investment) {
        for (LocalDate date = investment.getDate(); date.isBefore(LocalDate.now().plusDays(1)); date = date.plusDays(1))
        {
            final LocalDate day = date;

            Portfolio portfolio = portfolioRepository.findFirstByUserIdAndDate(investment.getUser().getId(), date);
            if (portfolio != null) {
                Collection<Investment> investments = investmentRepository.findByPortfoliosId(portfolio.getId());
                if (investments.stream().filter(i -> i.getId().equals(investment.getId())).findAny().isEmpty()) {
                    investments.add(investment);
                }
                portfolio.setInvestments(investments);

                portfolio.setValue(portfolio.getValue()
                        .add(investment.getQuantity().multiply(
                            assetService.getLatestAssetPriceForDate(investment.getAsset(), date)
                        ).setScale(8, RoundingMode.HALF_UP)));
            } else {
                Portfolio lastDaysPortfolio = portfolioRepository.findFirstByUserIdAndDate(investment.getUser().getId(), date.minusDays(1));
                if (lastDaysPortfolio != null) {
                    Collection<Investment> investments = investmentRepository.findByPortfoliosId(lastDaysPortfolio.getId());
                    if (investments.stream().filter(i -> i.getId().equals(investment.getId())).findAny().isEmpty()) {
                        investments.add(investment);
                    }

                    BigDecimal totalValue = investments.stream().map(previousDayInvestment ->
                            previousDayInvestment.getQuantity().multiply(
                                    assetService.getLatestAssetPriceForDate(previousDayInvestment.getAsset(), day)
                            ).setScale(8, RoundingMode.HALF_UP)
                    ).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP).setScale(8, RoundingMode.HALF_UP);

                    portfolio = Portfolio.builder()
                            .value(totalValue)
                            .investments(investments)
                            .date(date)
                            .user(investment.getUser()).build();
                } else {
                    portfolio = Portfolio.builder()
                            .value(investment.getQuantity().multiply(
                                    assetService.getLatestAssetPriceForDate(investment.getAsset(), date)
                            ).setScale(8, RoundingMode.HALF_UP))
                            .investments(Set.of(investment))
                            .date(date)
                            .user(investment.getUser()).build();
                }
            }

            log.info("saving portfolio for user: {} at: {}, total value: {}",
                    investment.getUser().getId(), date, portfolio.getValue());

            portfolioRepository.save(portfolio);
        }
    }
}
