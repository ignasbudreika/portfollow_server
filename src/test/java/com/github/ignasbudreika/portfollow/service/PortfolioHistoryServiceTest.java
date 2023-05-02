package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.DateValueDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDTO;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.*;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.PortfolioHistoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PortfolioHistoryServiceTest {
    private static final String ASSET_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final BigDecimal ASSET_PRICE = BigDecimal.TEN;
    private static final String ASSET_SYMBOL = "AAPL";
    private static final BigDecimal QUANTITY = BigDecimal.ONE;
    private static final String CONNECTION_ID = "43a29381-fd45-4fe7-8962-51973ca7ef9b";

    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private final AssetService assetService = mock(AssetService.class);
    private final StatisticsService statisticsService = mock(StatisticsService.class);
    private final PortfolioHistoryRepository portfolioHistoryRepository = mock(PortfolioHistoryRepository.class);
    private final InvestmentRepository investmentRepository = mock(InvestmentRepository.class);
    private final PortfolioHistoryService target =
            new PortfolioHistoryService(assetService, statisticsService, portfolioHistoryRepository, investmentRepository);

    @Test
    void shouldSaveLastKnownPortfolio_whenCurrentDayPortfolioDoesNotExist() {
        LocalDate date = LocalDate.now();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(date)
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(date).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(portfolioHistoryRepository.findFirstByUserIdAndDate(eq(USER_ID), any())).thenReturn(null);
        when(portfolioHistoryRepository.findFirstByUserIdAndDateBeforeOrderByDateDesc(eq(USER_ID), any()))
                .thenReturn(PortfolioHistory.builder()
                                .investments(List.of(investmentWithTx))
                                .build());
        when(portfolioHistoryRepository.save(any()))
                .thenReturn(PortfolioHistory.builder()
                        .user(User.builder().id(USER_ID).build())
                        .value(QUANTITY.multiply(ASSET_PRICE).setScale(2, RoundingMode.HALF_UP))
                        .investments(List.of(investmentWithTx))
                        .build());


        PortfolioHistory result = target.saveCurrentPortfolio(USER_ID);


        verify(portfolioHistoryRepository).findFirstByUserIdAndDate(eq(USER_ID), any());
        verify(portfolioHistoryRepository).findFirstByUserIdAndDateBeforeOrderByDateDesc(eq(USER_ID), any());
        verify(portfolioHistoryRepository).save(any());

        Assertions.assertEquals(USER_ID, result.getUser().getId());
        Assertions.assertEquals(QUANTITY.multiply(ASSET_PRICE).setScale(2, RoundingMode.HALF_UP), result.getValue());
    }

    @Test
    void shouldCreateEmptyPortfolio_whenNoPortfoliosExist() {
        when(portfolioHistoryRepository.findFirstByUserIdAndDate(eq(USER_ID), any())).thenReturn(null);
        when(portfolioHistoryRepository.findFirstByUserIdAndDateBeforeOrderByDateDesc(eq(USER_ID), any())).thenReturn(null);
        when(portfolioHistoryRepository.save(any()))
                .thenReturn(PortfolioHistory.builder()
                        .user(User.builder().id(USER_ID).build())
                        .value(BigDecimal.ZERO)
                        .investments(new ArrayList<>())
                        .build());


        PortfolioHistory result = target.saveCurrentPortfolio(USER_ID);


        verify(portfolioHistoryRepository).findFirstByUserIdAndDate(eq(USER_ID), any());
        verify(portfolioHistoryRepository).findFirstByUserIdAndDateBeforeOrderByDateDesc(eq(USER_ID), any());
        verify(portfolioHistoryRepository).save(any());

        Assertions.assertEquals(USER_ID, result.getUser().getId());
        Assertions.assertEquals(BigDecimal.ZERO, result.getValue());
    }

    @Test
    void shouldRecalculatePortfolioValue_whenTodaysPortfolioExists() {
        LocalDate date = LocalDate.now();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(date)
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(date).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(portfolioHistoryRepository.findFirstByUserIdAndDate(eq(USER_ID), any()))
                .thenReturn(PortfolioHistory.builder()
                        .investments(List.of(investmentWithTx))
                        .build());
        when(portfolioHistoryRepository.save(any()))
                .thenReturn(PortfolioHistory.builder()
                        .user(User.builder().id(USER_ID).build())
                        .value(QUANTITY.multiply(ASSET_PRICE).setScale(2, RoundingMode.HALF_UP))
                        .investments(new ArrayList<>())
                        .build());


        PortfolioHistory result = target.saveCurrentPortfolio(USER_ID);


        verify(portfolioHistoryRepository).findFirstByUserIdAndDate(eq(USER_ID), any());
        verify(portfolioHistoryRepository).save(any());

        Assertions.assertEquals(USER_ID, result.getUser().getId());
        Assertions.assertEquals(QUANTITY.multiply(ASSET_PRICE).setScale(2, RoundingMode.HALF_UP), result.getValue());

    }

    @Test
    void shouldInitPortfolio() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();


        target.initPortfolio(user);


        ArgumentCaptor<PortfolioHistory> captor = ArgumentCaptor.forClass(PortfolioHistory.class);
        verify(portfolioHistoryRepository, times(7)).save(captor.capture());
        Assertions.assertEquals(USER_ID, captor.getValue().getUser().getId());
        Assertions.assertEquals(BigDecimal.ZERO, captor.getValue().getValue());
    }

    @Test
    void shouldReturnEmptyPortfolio_whenUserPortfolioDoesNotExist() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(portfolioHistoryRepository.findFirstByUserIdOrderByDateDesc(USER_ID)).thenReturn(null);


        PortfolioDTO result = target.getUserPortfolio(user);


        verify(portfolioHistoryRepository).findFirstByUserIdOrderByDateDesc(USER_ID);

        Assertions.assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getTotalValue());
        Assertions.assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getTotalChange());
        Assertions.assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getTrend());
    }

    @Test
    void shouldReturnUserPortfolio() {
        LocalDate date = LocalDate.now();

        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        List<Investment> investments = List.of(Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(date)
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(date).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build());

        when(portfolioHistoryRepository.findFirstByUserIdOrderByDateDesc(USER_ID))
                .thenReturn(PortfolioHistory.builder()
                .investments(investments)
                .build());
        when(statisticsService.calculateTotalChange(anyCollection())).thenReturn(BigDecimal.TEN);
        when(statisticsService.calculateTrend(anyCollection())).thenReturn(BigDecimal.TEN);


        PortfolioDTO result = target.getUserPortfolio(user);


        verify(portfolioHistoryRepository).findFirstByUserIdOrderByDateDesc(USER_ID);
        verify(statisticsService).calculateTotalChange(anyCollection());
        verify(statisticsService).calculateTrend(anyCollection());

        Assertions.assertEquals(QUANTITY.multiply(ASSET_PRICE).setScale(2, RoundingMode.HALF_UP), result.getTotalValue());
        Assertions.assertEquals(BigDecimal.TEN, result.getTotalChange());
        Assertions.assertEquals(BigDecimal.TEN, result.getTrend());
    }

    @ParameterizedTest
    @EnumSource(value = HistoryType.class)
    void shouldReturnUserProfitLossHistory(HistoryType historyType) {
        LocalDate date = LocalDate.now();

        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        List<Investment> investments = List.of(Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(date)
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(date).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build());

        when(investmentRepository.findAllByUserId(USER_ID)).thenReturn(investments);
        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(asset.getPrice());


        List<DateValueDTO> result = target.getUserProfitLossHistory(user, historyType);


        verify(investmentRepository).findAllByUserId(USER_ID);

        Assertions.assertFalse(result.isEmpty());
        switch (historyType) {
            case WEEKLY -> Assertions.assertEquals(date.minusWeeks(1L), result.get(0).getDate());
            case MONTHLY -> Assertions.assertEquals(date.minusMonths(1L), result.get(0).getDate());
            case QUARTERLY -> Assertions.assertEquals(date.minusMonths(3L), result.get(0).getDate());
            case ALL -> Assertions.assertEquals(LocalDate.of(2023, 1, 1), result.get(0).getDate());
        }
        Assertions.assertEquals(BigDecimal.ZERO.setScale(8, RoundingMode.HALF_UP), result.get(result.size() - 1).getValue());
    }

    @ParameterizedTest
    @EnumSource(value = HistoryType.class)
    void shouldReturnUserPerformanceHistory(HistoryType historyType) {
        LocalDate date = LocalDate.now();

        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        List<Investment> investments = List.of(Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(date)
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(date).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build());

        when(investmentRepository.findAllByUserId(USER_ID)).thenReturn(investments);
        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(asset.getPrice());


        List<DateValueDTO> result = target.getUserPerformanceHistory(user, historyType);


        verify(investmentRepository).findAllByUserId(USER_ID);

        Assertions.assertFalse(result.isEmpty());
        switch (historyType) {
            case WEEKLY -> Assertions.assertEquals(date.minusWeeks(1L), result.get(0).getDate());
            case MONTHLY -> Assertions.assertEquals(date.minusMonths(1L), result.get(0).getDate());
            case QUARTERLY -> Assertions.assertEquals(date.minusMonths(3L), result.get(0).getDate());
            case ALL -> Assertions.assertEquals(LocalDate.of(2023, 1, 1), result.get(0).getDate());
        }
        Assertions.assertEquals(BigDecimal.ZERO, result.get(0).getValue());
    }

    @ParameterizedTest
    @EnumSource(value = HistoryType.class)
    void shouldReturnUserPortfolioHistory(HistoryType historyType) {
        LocalDate date = LocalDate.now();

        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        when(portfolioHistoryRepository.findAllByUserIdAndDateAfterOrderByDateAsc(eq(USER_ID), any())).thenReturn(
                List.of(PortfolioHistory.builder()
                        .date(date)
                        .value(QUANTITY.multiply(ASSET_PRICE))
                        .build()));


        List<DateValueDTO> result = target.getUserPortfolioHistory(user, historyType);


        verify(portfolioHistoryRepository).findAllByUserIdAndDateAfterOrderByDateAsc(eq(USER_ID), any());

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(date, result.get(0).getDate());
        Assertions.assertEquals(QUANTITY.multiply(ASSET_PRICE).setScale(2, RoundingMode.HALF_UP), result.get(result.size() - 1).getValue());
    }

    @Test
    void shouldCreatePortfolioHistoryBasedOnLastDaysPortfolio_whenLastDaysPortfolioExists() {
        LocalDate date = LocalDate.now();

        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investmentWithTx = Investment.builder()
                .id(UUID.randomUUID().toString())
                .symbol(ASSET_SYMBOL)
                .user(user)
                .asset(asset)
                .date(date)
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(date).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(portfolioHistoryRepository.findFirstByUserIdAndDate(eq(USER_ID), any())).thenReturn(null)
                        .thenReturn(PortfolioHistory.builder()
                                    .date(date)
                                    .value(QUANTITY.multiply(ASSET_PRICE))
                                    .investments(List.of(investmentWithTx))
                                    .build());
        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(asset.getPrice());


        target.createOrUpdatePortfolioHistory(investmentWithTx);


        verify(portfolioHistoryRepository, times(2)).findFirstByUserIdAndDate(eq(USER_ID), any());
        ArgumentCaptor<PortfolioHistory> captor = ArgumentCaptor.forClass(PortfolioHistory.class);
        verify(portfolioHistoryRepository).save(captor.capture());

        Assertions.assertEquals(investmentWithTx.getDate(), captor.getValue().getDate());
        Assertions.assertFalse( captor.getValue().getInvestments().isEmpty());
        Assertions.assertEquals(QUANTITY.multiply(ASSET_PRICE).setScale(8, RoundingMode.HALF_UP), captor.getValue().getValue());
    }

    @Test
    void shouldCreateNewPortfolioHistory() {
        LocalDate date = LocalDate.now();

        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investmentWithTx = Investment.builder()
                .id(UUID.randomUUID().toString())
                .symbol(ASSET_SYMBOL)
                .user(user)
                .asset(asset)
                .date(date)
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(date).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(portfolioHistoryRepository.findFirstByUserIdAndDate(eq(USER_ID), any())).thenReturn(null);
        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(asset.getPrice());


        target.createOrUpdatePortfolioHistory(investmentWithTx);


        verify(portfolioHistoryRepository, times(2)).findFirstByUserIdAndDate(eq(USER_ID), any());
        ArgumentCaptor<PortfolioHistory> captor = ArgumentCaptor.forClass(PortfolioHistory.class);
        verify(portfolioHistoryRepository).save(captor.capture());

        Assertions.assertEquals(investmentWithTx.getDate(), captor.getValue().getDate());
        Assertions.assertFalse( captor.getValue().getInvestments().isEmpty());
        Assertions.assertEquals(QUANTITY.multiply(ASSET_PRICE).setScale(8, RoundingMode.HALF_UP), captor.getValue().getValue());
    }

    @Test
    void shouldUpdatePortfolioHistory() {
        LocalDate date = LocalDate.now();

        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investmentWithTx = Investment.builder()
                .id(UUID.randomUUID().toString())
                .symbol(ASSET_SYMBOL)
                .user(user)
                .asset(asset)
                .date(date)
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(date).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(portfolioHistoryRepository.findFirstByUserIdAndDate(eq(USER_ID), any()))
                .thenReturn(PortfolioHistory.builder()
                        .date(date)
                        .value(QUANTITY.multiply(ASSET_PRICE))
                        .investments(List.of(investmentWithTx))
                        .build());
        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(asset.getPrice());


        target.createOrUpdatePortfolioHistory(investmentWithTx);


        verify(portfolioHistoryRepository).findFirstByUserIdAndDate(eq(USER_ID), any());
        ArgumentCaptor<PortfolioHistory> captor = ArgumentCaptor.forClass(PortfolioHistory.class);
        verify(portfolioHistoryRepository).save(captor.capture());

        Assertions.assertEquals(investmentWithTx.getDate(), captor.getValue().getDate());
        Assertions.assertFalse( captor.getValue().getInvestments().isEmpty());
        Assertions.assertEquals(QUANTITY.multiply(ASSET_PRICE).setScale(8, RoundingMode.HALF_UP), captor.getValue().getValue());

    }

    @Test
    void shouldUpdatePortfolioHistoryValue() {
        LocalDate date = LocalDate.now();

        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        List<Investment> investments = List.of(Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(date)
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(date).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build());

        when(portfolioHistoryRepository.findFirstByUserIdAndDate(USER_ID, date)).thenReturn(
                PortfolioHistory.builder()
                        .date(date)
                        .value(QUANTITY.multiply(ASSET_PRICE))
                        .investments(investments)
                        .build());
        when(assetService.getLatestAssetPriceForDate(asset, date)).thenReturn(ASSET_PRICE);


        target.updatePortfolioHistoryValue(user, date);


        verify(portfolioHistoryRepository).findFirstByUserIdAndDate(USER_ID, date);
        verify(assetService).getLatestAssetPriceForDate(asset, date);
        verify(portfolioHistoryRepository).save(any());
    }
}
