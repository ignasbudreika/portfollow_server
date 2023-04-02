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

    public Portfolio savePortfolio(String userId, BigDecimal value, Collection<Investment> investments, LocalDate date) {
        Portfolio portfolio = portfolioRepository.findFirstByUserIdAndDate(userId, date);
        if (portfolio != null) {
            portfolio.setValue(value);
            portfolio.setInvestments(investments);
            return portfolioRepository.save(portfolio);
        }

        return portfolioRepository.save(Portfolio.builder()
                .user(User.builder().id(userId).build())
                .value(value)
                .investments(investments)
                .date(date).build());
    }

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
            case ALL -> from = LocalDate.of(2023, 1, 1);
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
            Portfolio portfolio = portfolioRepository.findFirstByUserIdAndDate(investment.getUser().getId(), date);
            if (portfolio != null) {
                Collection<Investment> investments = investmentRepository.findByPortfoliosId(portfolio.getId());
                investments.add(investment);
                portfolio.setInvestments(investments);

                portfolio.setValue(portfolio.getValue()
                        .add(investment.getQuantity().multiply(
                            assetService.getLatestAssetPriceForDate(investment.getAsset(), date)
                        ).setScale(8, RoundingMode.HALF_UP)));
            } else {
                portfolio = Portfolio.builder()
                        .value(investment.getQuantity().multiply(
                                assetService.getLatestAssetPriceForDate(investment.getAsset(), date)
                        ).setScale(8, RoundingMode.HALF_UP))
                        .investments(Set.of(investment))
                        .date(date)
                        .user(investment.getUser()).build();
            }

            log.info("saving portfolio for user: {} at: {}, total value: {}",
                    investment.getUser().getId(), date, portfolio.getValue());

            portfolioRepository.save(portfolio);
        }
    }
}
