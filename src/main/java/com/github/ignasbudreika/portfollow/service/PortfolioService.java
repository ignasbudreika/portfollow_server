package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDistributionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioHistoryDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PortfolioService {
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private PortfolioRepository portfolioRepository;

    public Portfolio savePortfolio(String userId, BigDecimal value, LocalDateTime date) {
        return portfolioRepository.save(Portfolio.builder()
                .user(User.builder().id(userId).build())
                .value(value)
                .date(date).build());
    }

    public PortfolioDTO getUserPortfolio(User user) {
        BigDecimal totalValue = investmentService.getTotalValueByUserId(user.getId());

        Portfolio lastDaysPortfolio = portfolioRepository.findFirstByDateBetweenOrderByDateAsc(
                LocalDateTime.now().minusDays(1L).truncatedTo(ChronoUnit.MILLIS), LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));

        return PortfolioDTO.builder()
                .totalValue(totalValue)
                .change(totalValue.subtract(lastDaysPortfolio.getValue())
                        .divide(lastDaysPortfolio.getValue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100L).setScale(2, RoundingMode.HALF_UP)))
                .build();
    }

    public List<PortfolioDistributionDTO> getUserPortfolioDistribution(User user) {
        Collection<InvestmentDTO> investments = investmentService.getUserInvestments(user);

        BigDecimal totalValue = investments.stream().map(InvestmentDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<InvestmentType, List<InvestmentDTO>> investmentsByType = investments.stream().collect(Collectors.groupingBy(InvestmentDTO::getType));

        List<PortfolioDistributionDTO> distribution = investmentsByType.entrySet().stream().map(typeInvestments -> {
            BigDecimal value = typeInvestments.getValue().stream().map(InvestmentDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);

            return PortfolioDistributionDTO.builder()
                    .label(typeInvestments.getKey().toString())
                    .value(value)
                    .percentage(value
                            .divide(totalValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                    .build();
        }).toList();

        return distribution;
    }

    public List<PortfolioDistributionDTO> getUserPortfolioDistributionByType(User user, InvestmentType type) {
        Collection<InvestmentDTO> investments = investmentService.getUserInvestmentsByType(user, type);

        BigDecimal totalValue = investments.stream().map(InvestmentDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PortfolioDistributionDTO> distribution = investments.stream().map(investment ->
                PortfolioDistributionDTO.builder()
                    .label(investment.getSymbol())
                    .value(investment.getValue())
                    .percentage(investment.getValue()
                            .divide(totalValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                    .build()
        ).toList();

        return distribution;
    }

    public List<PortfolioHistoryDTO> getUserPortfolioHistory(User user) {
        Collection<Portfolio> portfolios = portfolioRepository.findAllByUserIdOrderByDateAsc(user.getId());

        return portfolios.stream().map(portfolio -> PortfolioHistoryDTO.builder()
                .value(portfolio.getValue().setScale(2, RoundingMode.HALF_UP))
                // todo return timestamp
                .time(String.valueOf(portfolio.getDate().truncatedTo(ChronoUnit.MINUTES).toEpochSecond(ZoneOffset.UTC))).build())
                .toList();
    }
}
