package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
        Collection<InvestmentDTO> investments = investmentService.getUserInvestments(user);

        BigDecimal totalValue = investments.stream().map(InvestmentDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<InvestmentType, List<InvestmentDTO>> investmentsByType = investments.stream().collect(Collectors.groupingBy(InvestmentDTO::getType));

        List<PortfolioDTO.DistributionDTO> distribution = investmentsByType.entrySet().stream().map(typeInvestments -> {
            BigDecimal value = typeInvestments.getValue().stream().map(InvestmentDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);

            return PortfolioDTO.DistributionDTO.builder()
                    .label(typeInvestments.getKey().toString())
                    .value(value)
                    .percentage(value
                            .divide(totalValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                    .build();
        }).toList();

        return PortfolioDTO.builder().totalValue(totalValue).distribution(distribution).build();
    }

    public PortfolioDTO getUserPortfolioByType(User user, InvestmentType type) {
        Collection<InvestmentDTO> investments = investmentService.getUserInvestmentsByType(user, type);

        BigDecimal totalValue = investments.stream().map(InvestmentDTO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PortfolioDTO.DistributionDTO> distribution = investments.stream().map(investment ->
            PortfolioDTO.DistributionDTO.builder()
                    .label(investment.getSymbol())
                    .value(investment.getValue())
                    .percentage(investment.getValue()
                            .divide(totalValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                    .build()
        ).toList();

        return PortfolioDTO.builder().totalValue(totalValue).distribution(distribution).build();
    }
}
