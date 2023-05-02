package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

class PortfolioServiceTest {
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final PortfolioHistoryService historyService = mock(PortfolioHistoryService.class);
    private final PortfolioService target = new PortfolioService(portfolioRepository, historyService);

    @Test
    void shouldCreatePortfolio() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(portfolioRepository.existsByUserId(user.getId())).thenReturn(false);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(Portfolio.builder()
                .user(user).build());


        Portfolio result = target.createPortfolio(user);


        verify(portfolioRepository).existsByUserId(user.getId());
        ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
        verify(portfolioRepository).save(captor.capture());
        Assertions.assertEquals(user, captor.getValue().getUser());
        Assertions.assertFalse(captor.getValue().isPublished());
        Assertions.assertTrue(captor.getValue().isHiddenValue());
        verify(historyService).initPortfolio(any(User.class));

        Assertions.assertEquals(user, result.getUser());
    }
}
