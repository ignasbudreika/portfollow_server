package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.*;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Service
public class PublicPortfolioService {
    private static final int LIMIT = 3;

    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private InvestmentRepository investmentRepository;
    @Autowired PortfolioHistoryService portfolioHistoryService;

    public PublicPortfolioListDTO getPublicPortfolios(int index) {
        Page<Portfolio> portfolios = portfolioRepository.findAllByPublished(true, PageRequest.of(index, LIMIT));

        PublicPortfolioDTO[] portfolioDTOs = portfolios.stream().map(portfolio -> {
            return PublicPortfolioDTO.builder()
                    .id(portfolio.getId())
                    .title(portfolio.getTitle())
                    .description(portfolio.getDescription())
                    .history(portfolio.isHiddenValue() ?
                            portfolioHistoryService.getUserProfitLossHistory(portfolio.getUser(), HistoryType.MONTHLY).toArray(DateValueDTO[]::new) :
                            portfolioHistoryService.getUserPerformanceHistory(portfolio.getUser(), HistoryType.MONTHLY).toArray(DateValueDTO[]::new)).build();
        }).toArray(PublicPortfolioDTO[]::new);

        return PublicPortfolioListDTO.builder()
                .more(index+1 < portfolios.getTotalPages())
                .index(index+1)
                .portfolios(portfolioDTOs).build();
    }

    public PublicPortfolioStatisticsDTO getPublicPortfolioStats(String id) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!portfolio.isPublished()) {
            throw new EntityNotFoundException();
        }

        Collection<Investment> investments = investmentRepository.findAllByUserId(portfolio.getUser().getId());

        BigDecimal trend = portfolioHistoryService.calculateTrend(investments);
        BigDecimal change;
        if (portfolio.isHiddenValue()) {
            change = portfolioHistoryService.calculateTotalPerformance(investments);
        } else {
            change = portfolioHistoryService.calculateTotalChange(investments);
        }
        List<PortfolioDistributionDTO> distribution = portfolioHistoryService.getUserPortfolioDistribution(portfolio.getUser());
        if (portfolio.isHiddenValue()) {
            distribution = distribution.stream().map(entry -> PortfolioDistributionDTO.builder()
                    .label(entry.getLabel())
                    .percentage(entry.getPercentage()).build()).toList();
        }

        return PublicPortfolioStatisticsDTO.builder()
                .hiddenValue(portfolio.isHiddenValue())
                .trend(trend)
                .totalChange(change)
                .distribution(distribution.toArray(PortfolioDistributionDTO[]::new)).build();
    }
}
