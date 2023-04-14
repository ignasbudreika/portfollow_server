package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.DateValueDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PublicPortfolioDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PublicPortfolioListDTO;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PublicPortfolioService {
    private static final int LIMIT = 3;

    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired PortfolioHistoryService portfolioHistoryService;

    public PublicPortfolioListDTO getPublicPortfolios(int index) {
        Page<Portfolio> portfolios = portfolioRepository.findAllByPublished(true, PageRequest.of(index, LIMIT));

        PublicPortfolioDTO[] portfolioDTOs = portfolios.stream().map(portfolio -> {
            return PublicPortfolioDTO.builder()
                    .id(portfolio.getId())
                    .title(portfolio.getTitle())
                    .description(portfolio.getDescription())
                    .history(portfolioHistoryService.getUserPortfolioHistory(portfolio.getUser(), HistoryType.MONTHLY).toArray(DateValueDTO[]::new)).build();
        }).toArray(PublicPortfolioDTO[]::new);

        return PublicPortfolioListDTO.builder()
                .more(index+1 < portfolios.getTotalPages())
                .index(index+1)
                .portfolios(portfolioDTOs).build();
    }


}
