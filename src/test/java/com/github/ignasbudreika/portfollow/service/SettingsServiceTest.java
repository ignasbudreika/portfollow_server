package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.SettingsUpdateDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.SettingsDTO;
import com.github.ignasbudreika.portfollow.model.Portfolio;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.PortfolioRepository;
import com.github.ignasbudreika.portfollow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SettingsServiceTest {
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private static final String PORTFOLIO_ID = "eb15a63f-c248-44d3-895e-0e7b19799d42";
    private static final String PORTFOLIO_TITLE = "portfolio";
    private static final String PORTFOLIO_DESCRIPTION = "description";
    private static final boolean PORTFOLIO_IS_PUBLIC = true;
    private static final boolean PORTFOLIO_REVEAL_VALUE = false;

    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SettingsService target = new SettingsService(portfolioRepository, userRepository);

    @Test
    void shouldGetUserSettings() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        when(portfolioRepository.findByUserId(user.getId())).thenReturn(Portfolio.builder()
                .id(PORTFOLIO_ID)
                .title(PORTFOLIO_TITLE)
                .description(PORTFOLIO_DESCRIPTION)
                .published(PORTFOLIO_IS_PUBLIC)
                .hiddenValue(!PORTFOLIO_REVEAL_VALUE).build());


        SettingsDTO result = target.getUserSettings(user);


        verify(portfolioRepository).findByUserId(user.getId());

        assertEquals(USER_EMAIL, result.getUserInfo().getEmail());
        assertEquals(USER_USERNAME, result.getUserInfo().getUsername());
        assertEquals(PORTFOLIO_ID, result.getPortfolioInfo().getId());
        assertEquals(PORTFOLIO_TITLE, result.getPortfolioInfo().getTitle());
        assertEquals(PORTFOLIO_DESCRIPTION, result.getPortfolioInfo().getDescription());
        assertEquals(PORTFOLIO_IS_PUBLIC, result.getPortfolioInfo().isPublic());
        assertEquals(PORTFOLIO_REVEAL_VALUE, result.getPortfolioInfo().isRevealValue());
    }

    @Test
    void shouldUpdateUsername_whenUsernameIsNotEmpty() {
        SettingsUpdateDTO settingsUpdate = SettingsUpdateDTO.builder().username(USER_USERNAME).build();
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username("John").build();
        when(portfolioRepository.findByUserId(user.getId())).thenReturn(Portfolio.builder()
                .id(PORTFOLIO_ID)
                .title(PORTFOLIO_TITLE)
                .description(PORTFOLIO_DESCRIPTION)
                .published(PORTFOLIO_IS_PUBLIC)
                .hiddenValue(!PORTFOLIO_REVEAL_VALUE).build());


        SettingsDTO result = target.updateUserSettings(settingsUpdate, user);


        verify(portfolioRepository).findByUserId(USER_ID);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(USER_USERNAME, captor.getValue().getUsername());

        assertEquals(USER_USERNAME, result.getUserInfo().getUsername());
    }

    @Test
    void shouldUpdatePortfolioTitle_whenTitleIsNotEmpty() {
        SettingsUpdateDTO settingsUpdate = SettingsUpdateDTO.builder().title(PORTFOLIO_TITLE).build();
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();
        when(portfolioRepository.findByUserId(user.getId())).thenReturn(Portfolio.builder()
                .id(PORTFOLIO_ID)
                .title("other title")
                .description(PORTFOLIO_DESCRIPTION)
                .published(PORTFOLIO_IS_PUBLIC)
                .hiddenValue(!PORTFOLIO_REVEAL_VALUE).build());


        SettingsDTO result = target.updateUserSettings(settingsUpdate, user);


        verify(portfolioRepository).findByUserId(USER_ID);

        ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
        verify(portfolioRepository).save(captor.capture());
        assertEquals(PORTFOLIO_TITLE, captor.getValue().getTitle());

        assertEquals(PORTFOLIO_TITLE, result.getPortfolioInfo().getTitle());
    }

    @Test
    void shouldUpdateSettings() {
        SettingsUpdateDTO settingsUpdate = SettingsUpdateDTO.builder()
                .description(PORTFOLIO_DESCRIPTION)
                .isPublic(PORTFOLIO_IS_PUBLIC)
                .hideValue(!PORTFOLIO_REVEAL_VALUE).build();
        User user = User.builder().id(USER_ID).build();
        when(portfolioRepository.findByUserId(user.getId())).thenReturn(Portfolio.builder()
                .id(PORTFOLIO_ID)
                .title(PORTFOLIO_TITLE)
                .description("")
                .published(!PORTFOLIO_IS_PUBLIC)
                .hiddenValue(PORTFOLIO_REVEAL_VALUE).build());


        SettingsDTO result = target.updateUserSettings(settingsUpdate, user);


        verify(portfolioRepository).findByUserId(USER_ID);

        ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
        verify(portfolioRepository).save(captor.capture());
        assertEquals(settingsUpdate.getDescription(), captor.getValue().getDescription());
        assertEquals(settingsUpdate.isPublic(), captor.getValue().isPublished());
        assertEquals(settingsUpdate.isHideValue(), captor.getValue().isHiddenValue());

        assertEquals(settingsUpdate.getDescription(), result.getPortfolioInfo().getDescription());
        assertEquals(settingsUpdate.isPublic(), result.getPortfolioInfo().isPublic());
        assertEquals(!settingsUpdate.isHideValue(), result.getPortfolioInfo().isRevealValue());
    }
}
