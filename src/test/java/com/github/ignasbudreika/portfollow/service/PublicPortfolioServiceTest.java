package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CommentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.*;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.Comment;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.CommentRepository;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class PublicPortfolioServiceTest {
    private static final String COMMENT_ID = "a6635844-cb6f-49ac-bdab-c78575f50825";
    private static final String PORTFOLIO_ID = "11111111-cb6f-49ac-bdab-c78575f50825";
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private static final String DISTRIBUTION_LABEL = "ETH";
    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final StatisticsService statisticsService = mock(StatisticsService.class);
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final InvestmentRepository investmentRepository = mock(InvestmentRepository.class);
    private final PortfolioHistoryService portfolioHistoryService = mock(PortfolioHistoryService.class);
    private final PublicPortfolioService target = new PublicPortfolioService(portfolioRepository, statisticsService, commentRepository, investmentRepository, portfolioHistoryService);

    @Test
    void shouldReturnPublicPortfolios() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Portfolio portfolio = Portfolio.builder()
                .title("portfolio")
                .description("description")
                .hiddenValue(false)
                .published(true)
                .user(user).id(PORTFOLIO_ID).build();

        when(portfolioRepository.findAllByPublished(eq(true), any())).thenReturn(new PageImpl<>(List.of(portfolio)));
        when(portfolioHistoryService.getUserProfitLossHistory(user, HistoryType.MONTHLY))
                .thenReturn(List.of(DateValueDTO.builder().date(LocalDate.now()).value(BigDecimal.TEN).build()));


        PublicPortfolioListDTO result = target.getPublicPortfolios(0);


        verify(portfolioRepository).findAllByPublished(eq(true), any());
        verify(portfolioHistoryService).getUserProfitLossHistory(user, HistoryType.MONTHLY);

        Assertions.assertEquals(1, result.getPortfolios().length);
        Assertions.assertEquals(portfolio.getTitle(), result.getPortfolios()[0].getTitle());
        Assertions.assertEquals(portfolio.getDescription(), result.getPortfolios()[0].getDescription());
        Assertions.assertEquals(1, result.getPortfolios()[0].getHistory().length);
    }

    @Test
    void shouldReturnPublicPortfolioById() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Portfolio portfolio = Portfolio.builder()
                .title("portfolio")
                .description("description")
                .hiddenValue(false)
                .published(true)
                .user(user).id(PORTFOLIO_ID).build();

        when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(portfolio));
        when(portfolioHistoryService.getUserProfitLossHistory(user, HistoryType.MONTHLY))
                .thenReturn(List.of(DateValueDTO.builder().date(LocalDate.now()).value(BigDecimal.TEN).build()));


        PublicPortfolioDTO result = target.getPublicPortfolio(PORTFOLIO_ID);


        verify(portfolioRepository).findById(PORTFOLIO_ID);
        verify(portfolioHistoryService).getUserProfitLossHistory(user, HistoryType.MONTHLY);

        Assertions.assertEquals(portfolio.getTitle(), result.getTitle());
        Assertions.assertEquals(portfolio.getDescription(), result.getDescription());
        Assertions.assertEquals(1, result.getHistory().length);
    }

    @Test
    void shouldReturnPublicPortfolioStats() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        List<Investment> investments = List.of(new Investment());

        Portfolio portfolio = Portfolio.builder().hiddenValue(true).published(true).user(user).id(PORTFOLIO_ID).build();

        when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(portfolio));
        when(investmentRepository.findAllByUserId(user.getId())).thenReturn(investments);
        when(statisticsService.calculateTrend(investments)).thenReturn(BigDecimal.ONE);
        when(statisticsService.calculateTotalPerformance(investments)).thenReturn(BigDecimal.TEN);
        when(statisticsService.getUserPortfolioDistribution(user)).thenReturn(List.of(PortfolioDistributionDTO.builder()
                .percentage(new BigDecimal("100"))
                .value(BigDecimal.TEN)
                .label(DISTRIBUTION_LABEL).build()));


        PublicPortfolioStatisticsDTO result = target.getPublicPortfolioStats(PORTFOLIO_ID);


        verify(portfolioRepository).findById(portfolio.getId());
        verify(statisticsService).getUserPortfolioDistribution(user);
        verify(statisticsService).calculateTrend(investments);
        verify(statisticsService).calculateTotalPerformance(investments);
        verify(investmentRepository).findAllByUserId(user.getId());

        Assertions.assertTrue(result.isHiddenValue());
        Assertions.assertEquals(1, result.getDistribution().length);
        Assertions.assertEquals(BigDecimal.ONE, result.getTrend());
        Assertions.assertEquals(BigDecimal.TEN, result.getTotalChange());
    }

    @Test
    void shouldReturnPublicPortfolioDistribution() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Portfolio portfolio = Portfolio.builder().hiddenValue(true).published(true).user(user).id(PORTFOLIO_ID).build();

        when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(portfolio));
        when(statisticsService.getUserPortfolioDistribution(user)).thenReturn(List.of(PortfolioDistributionDTO.builder()
                .percentage(new BigDecimal("100"))
                .value(BigDecimal.TEN)
                .label(DISTRIBUTION_LABEL).build()));


        PublicPortfolioDistributionDTO result = target.getPublicPortfolioDistribution(PORTFOLIO_ID, null);


        verify(portfolioRepository).findById(portfolio.getId());
        verify(statisticsService).getUserPortfolioDistribution(user);

        Assertions.assertTrue(result.isHiddenValue());
        Assertions.assertEquals(1, result.getDistribution().length);
        Assertions.assertEquals(DISTRIBUTION_LABEL, result.getDistribution()[0].getLabel());
        Assertions.assertNull(result.getDistribution()[0].getValue());
        Assertions.assertEquals(new BigDecimal("100"), result.getDistribution()[0].getPercentage());
    }

    @Test
    void shouldAddComment() throws BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        CommentDTO comment = CommentDTO.builder()
                .comment("comment").build();

        Portfolio portfolio = Portfolio.builder().published(true).user(user).id(PORTFOLIO_ID).build();

        when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(portfolio));


        target.comment(user, PORTFOLIO_ID, comment);


        verify(portfolioRepository).findById(portfolio.getId());
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        Assertions.assertEquals(comment.getComment(), captor.getValue().getComment());
        Assertions.assertEquals(user, captor.getValue().getUser());
        Assertions.assertEquals(portfolio, captor.getValue().getPortfolio());
    }

    @Test
    void shouldReturnPortfolioComments() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Comment comment = Comment.builder()
                .id(COMMENT_ID)
                .comment("comment")
                .user(user).build();

        Portfolio portfolio = Portfolio.builder().published(true).user(user).id(PORTFOLIO_ID).build();

        when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(portfolio));
        when(commentRepository.findAllByPortfolioId(portfolio.getId())).thenReturn(List.of(comment));


        AuthorCommentDTO[] result = target.getPortfolioComments(user, PORTFOLIO_ID);


        verify(portfolioRepository).findById(portfolio.getId());
        verify(commentRepository).findAllByPortfolioId(portfolio.getId());

        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(comment.getComment(), result[0].getComment());
        Assertions.assertEquals(user.getUsername(), result[0].getAuthor());
        Assertions.assertTrue(result[0].isDeletable());
    }

    @Test
    void shouldDeleteComment() throws BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Comment comment = Comment.builder()
                .id(COMMENT_ID)
                .comment("comment")
                .user(user).build();

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));


        target.deleteComment(user, COMMENT_ID);


        verify(commentRepository).findById(COMMENT_ID);
        verify(commentRepository).delete(comment);
    }
}
