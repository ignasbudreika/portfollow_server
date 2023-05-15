package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CommentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.*;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.Comment;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.CommentRepository;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PublicPortfolioService {
    private static final int LIMIT = 3;
    private PortfolioRepository portfolioRepository;
    private StatisticsService statisticsService;
    private CommentRepository commentRepository;
    private InvestmentRepository investmentRepository;
    private PortfolioHistoryService portfolioHistoryService;

    public PublicPortfolioListDTO getPublicPortfolios(int index) {
        Page<Portfolio> portfolios = portfolioRepository.findAllByPublished(true, PageRequest.of(index, LIMIT));

        PublicPortfolioDTO[] portfolioDTOs = portfolios.stream().map(portfolio ->
            PublicPortfolioDTO.builder()
                    .id(portfolio.getId())
                    .title(portfolio.getTitle())
                    .description(portfolio.getDescription())
                    .history(portfolio.isHiddenValue() ?
                            portfolioHistoryService.getUserPerformanceHistory(portfolio.getUser(), HistoryType.MONTHLY).toArray(DateValueDTO[]::new) :
                            portfolioHistoryService.getUserProfitLossHistory(portfolio.getUser(), HistoryType.MONTHLY).toArray(DateValueDTO[]::new))
                    .build()
        ).toArray(PublicPortfolioDTO[]::new);

        return PublicPortfolioListDTO.builder()
                .more(index+1 < portfolios.getTotalPages())
                .index(index+1)
                .portfolios(portfolioDTOs).build();
    }

    public PublicPortfolioDTO getPublicPortfolio(String id) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        return PublicPortfolioDTO.builder()
                .id(portfolio.getId())
                .title(portfolio.getTitle())
                .description(portfolio.getDescription())
                .history(portfolio.isHiddenValue() ?
                        portfolioHistoryService.getUserPerformanceHistory(portfolio.getUser(), HistoryType.MONTHLY).toArray(DateValueDTO[]::new) :
                        portfolioHistoryService.getUserProfitLossHistory(portfolio.getUser(), HistoryType.MONTHLY).toArray(DateValueDTO[]::new))
                .build();
    }

    public PublicPortfolioStatisticsDTO getPublicPortfolioStats(String id) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!portfolio.isPublished()) {
            throw new EntityNotFoundException();
        }

        Collection<Investment> investments = investmentRepository.findAllByUserId(portfolio.getUser().getId());

        BigDecimal trend = statisticsService.calculateTrend(investments);
        BigDecimal change;
        if (portfolio.isHiddenValue()) {
            change = statisticsService.calculateTotalPerformance(investments);
        } else {
            change = statisticsService.calculateTotalChange(investments);
        }
        List<PortfolioDistributionDTO> distribution = statisticsService.getUserPortfolioDistribution(portfolio.getUser());
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

    public PublicPortfolioDistributionDTO getPublicPortfolioDistribution(String id, String type) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!portfolio.isPublished()) {
            throw new EntityNotFoundException();
        }

        List<PortfolioDistributionDTO> distribution = StringUtils.isNotBlank(type) ?
                statisticsService.getUserPortfolioDistributionByType(portfolio.getUser(), InvestmentType.valueOf(type)) :
                statisticsService.getUserPortfolioDistribution(portfolio.getUser());
        if (portfolio.isHiddenValue()) {
            distribution = distribution.stream().map(entry -> PortfolioDistributionDTO.builder()
                    .label(entry.getLabel())
                    .percentage(entry.getPercentage()).build()).toList();
        }

        return PublicPortfolioDistributionDTO.builder()
                .hiddenValue(portfolio.isHiddenValue())
                .distribution(distribution.toArray(PortfolioDistributionDTO[]::new)).build();
    }

    public void comment(User user, String id, CommentDTO comment) throws BusinessLogicException {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!portfolio.isPublished()) {
            throw new EntityNotFoundException();
        }

        if (StringUtils.isNotBlank(portfolio.getAllowedUsers())
                && Arrays.stream(portfolio.getAllowedUsers().split(",")).filter(allowed -> allowed.equals(user.getEmail())).findFirst().isEmpty()) {
            throw new BusinessLogicException(String.format("user: %s is not in allowed users list for portfolio: %s", user.getId(), portfolio.getId()));
        }

        commentRepository.save(Comment.builder()
                .comment(comment.getComment())
                .portfolio(portfolio)
                .user(user).build());
    }

    public AuthorCommentDTO[] getPortfolioComments(User user, String id) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!portfolio.isPublished()) {
            throw new EntityNotFoundException();
        }

        return commentRepository.findAllByPortfolioId(portfolio.getId()).stream().map(comment ->
            AuthorCommentDTO.builder()
                    .id(comment.getId())
                    .author(comment.getUser().getUsername())
                    .comment(comment.getComment())
                    .deletable(user.getId().equals(comment.getUser().getId()) || user.getId().equals(portfolio.getUser().getId())).build()
        ).toArray(AuthorCommentDTO[]::new);
    }

    public void deleteComment(User user, String id) throws BusinessLogicException {
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isEmpty()) {
            return;
        }

        if (!comment.get().getUser().getId().equals(user.getId()) && !comment.get().getPortfolio().getUser().getId().equals(user.getId())) {
            throw new BusinessLogicException(String.format("user: %s cannot delete comment: %s", user.getId(), comment.get().getId()));
        }

        commentRepository.delete(comment.get());
    }
}
