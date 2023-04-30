package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.DateValueDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioDistributionDTO;
import com.github.ignasbudreika.portfollow.enums.HistoryType;
import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.*;
import com.github.ignasbudreika.portfollow.repository.PortfolioHistoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StatisticsServiceTest {
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private static final String ASSET_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final BigDecimal QUANTITY = BigDecimal.ONE;
    private static final BigDecimal ASSET_PRICE = BigDecimal.TEN;
    private static final String ASSET_SYMBOL = "ETH";
    private final AssetService assetService = mock(AssetService.class);
    private final PortfolioHistoryRepository portfolioHistoryRepository = mock(PortfolioHistoryRepository.class);
    private final StatisticsService target = new StatisticsService(assetService, portfolioHistoryRepository);

    @Test
    void shouldGetAssetDayTrend() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(BigDecimal.ONE);


        BigDecimal result = target.getAssetDayTrend(asset);


        verify(assetService).getLatestAssetPriceForDate(eq(asset), any());
        Assertions.assertEquals(BigDecimal.valueOf(900).setScale(4, RoundingMode.HALF_UP), result);
    }

    @Test
    void shouldGetInvestmentTotalChange() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();
        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(BigDecimal.ONE);


        BigDecimal result = target.getInvestmentTotalChange(investmentWithTx);


        verify(assetService).getLatestAssetPriceForDate(eq(asset), any());
        Assertions.assertEquals(BigDecimal.valueOf(9).setScale(8, RoundingMode.HALF_UP), result);
    }

    @Test
    void shouldCalculateTotalInvestmentsChange() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();
        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();
        Investment secondInvestmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(BigDecimal.ONE);


        BigDecimal result = target.calculateTotalChange(List.of(investmentWithTx, secondInvestmentWithTx));


        verify(assetService, times(2)).getLatestAssetPriceForDate(eq(asset), any());
        Assertions.assertEquals(BigDecimal.valueOf(18).setScale(8, RoundingMode.HALF_UP), result);
    }

    @Test
    void shouldCalculateTrend() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();
        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();
        Investment secondInvestmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(BigDecimal.ONE);


        BigDecimal result = target.calculateTrend(List.of(investmentWithTx, secondInvestmentWithTx));


        verify(assetService, times(2)).getLatestAssetPriceForDate(eq(asset), any());
        Assertions.assertEquals(BigDecimal.valueOf(900).setScale(6, RoundingMode.HALF_UP), result);
    }

    @Test
    void shouldCalculateTotalPerformance() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();
        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();
        Investment secondInvestmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(BigDecimal.ONE);


        BigDecimal result = target.calculateTotalPerformance(List.of(investmentWithTx, secondInvestmentWithTx));


        verify(assetService, times(2)).getLatestAssetPriceForDate(eq(asset), any());
        Assertions.assertEquals(BigDecimal.valueOf(900).setScale(6, RoundingMode.HALF_UP), result);
    }

    @ParameterizedTest
    @EnumSource(value = HistoryType.class)
    void shouldReturnComparisonPerformanceHistory(HistoryType type) {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol("SPY")
                .type(InvestmentType.STOCK).build();

        when(assetService.getAsset("SPY", InvestmentType.STOCK)).thenReturn(asset);
        when(assetService.getLatestAssetPriceForDate(eq(asset), any())).thenReturn(BigDecimal.ONE).thenReturn(BigDecimal.TEN);


        List<DateValueDTO> result = target.getComparisonPerformanceHistory(type);


        verify(assetService).getAsset(asset.getSymbol(), asset.getType());
        verify(assetService, atLeast(2)).getLatestAssetPriceForDate(eq(asset), any());
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void shouldReturnPortfolioDistribution() {
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
                .symbol(ASSET_SYMBOL)
                .asset(asset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        PortfolioHistory portfolioHistory = PortfolioHistory.builder()
                .user(user)
                .investments(List.of(investmentWithTx))
                .value(BigDecimal.TEN).build();

        when(portfolioHistoryRepository.findFirstByUserIdOrderByDateDesc(user.getId())).thenReturn(portfolioHistory);


        List<PortfolioDistributionDTO> result = target.getUserPortfolioDistribution(user);


        verify(portfolioHistoryRepository).findFirstByUserIdOrderByDateDesc(user.getId());

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(asset.getType().toString(), result.get(0).getLabel());
        Assertions.assertEquals(BigDecimal.TEN, result.get(0).getValue());
        Assertions.assertEquals(new BigDecimal("100").setScale(2, RoundingMode.HALF_UP), result.get(0).getPercentage());
    }

    @Test
    void shouldReturnPortfolioDistributionByType() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();
        Asset stockAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol("AAPL")
                .type(InvestmentType.STOCK).build();
        Investment cryptoInvestmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(cryptoAsset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();
        Investment stockInvestmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .asset(stockAsset)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.STOCK)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        PortfolioHistory portfolioHistory = PortfolioHistory.builder()
                .user(user)
                .investments(List.of(cryptoInvestmentWithTx, stockInvestmentWithTx))
                .value(BigDecimal.TEN).build();

        when(portfolioHistoryRepository.findFirstByUserIdOrderByDateDesc(user.getId())).thenReturn(portfolioHistory);


        List<PortfolioDistributionDTO> result = target.getUserPortfolioDistributionByType(user, InvestmentType.CRYPTO);


        verify(portfolioHistoryRepository).findFirstByUserIdOrderByDateDesc(user.getId());

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(cryptoAsset.getSymbol(), result.get(0).getLabel());
        Assertions.assertEquals(BigDecimal.TEN, result.get(0).getValue());
        Assertions.assertEquals(new BigDecimal("100").setScale(2, RoundingMode.HALF_UP), result.get(0).getPercentage());

    }
}
