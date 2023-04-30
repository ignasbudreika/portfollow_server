package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CurrencyDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.CurrencyInvestmentDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentStatsDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.Asset;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import com.github.ignasbudreika.portfollow.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CurrencyServiceTest {
    private static final String ASSET_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final BigDecimal ASSET_PRICE = BigDecimal.TEN;
    private static final String ASSET_SYMBOL = "AAPL";
    private static final String CURRENCY_SYMBOL = "EUR";
    private static final BigDecimal CURRENCY_QUANTITY = BigDecimal.TEN;
    private static final String USER_ID = "56d1dd5d-ebb4-45da-84d3-92f7e21abe08";

    private final StatisticsService statisticsService = mock(StatisticsService.class);
    private final InvestmentService investmentService = mock(InvestmentService.class);
    private final PortfolioHistoryService portfolioHistoryService = mock(PortfolioHistoryService.class);
    private final CurrencyService target = new CurrencyService(statisticsService, investmentService, portfolioHistoryService);

    @Test
    void shouldCreateCurrencyInvestment() throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        User user = User.builder().id(USER_ID).build();
        CurrencyDTO currency = CurrencyDTO.builder()
                .date(LocalDate.now())
                .quantity(CURRENCY_QUANTITY)
                .symbol(CURRENCY_SYMBOL)
                .crypto(false).build();
        Investment investment = Investment.builder()
                .date(currency.getDate())
                .quantity(currency.getQuantity())
                .symbol(currency.getSymbol()).build();

        when(investmentService.createInvestment(any(), eq(user))).thenReturn(investment);


        CurrencyInvestmentDTO result = target.createCurrencyInvestment(currency, user);


        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(investmentService).createInvestment(any(), captor.capture());
        Assertions.assertEquals(user.getId(), captor.getValue().getId());

        Assertions.assertEquals(investment.getSymbol(), result.getSymbol());
    }

    @Test
    void shouldGetCurrencyInvestments() {
        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();
        Asset fiatAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.FIAT).build();
        InvestmentTransaction tx = InvestmentTransaction.builder()
                .type(InvestmentTransactionType.BUY)
                .date(LocalDate.now())
                .quantity(BigDecimal.TEN).build();
        Investment cryptoInvestment = Investment.builder()
                .date(LocalDate.now())
                .asset(cryptoAsset)
                .symbol(CURRENCY_SYMBOL)
                .type(InvestmentType.CRYPTO)
                .updateType(InvestmentUpdateType.MANUAL)
                .transactions(Set.of(tx)).build();
        List cryptoInvestments = new ArrayList();
        cryptoInvestments.add(cryptoInvestment);
        Investment fiatInvestment = Investment.builder()
                .date(LocalDate.now())
                .asset(fiatAsset)
                .symbol(CURRENCY_SYMBOL)
                .type(InvestmentType.FIAT)
                .updateType(InvestmentUpdateType.MANUAL)
                .transactions(Set.of(tx)).build();
        List fiatInvestments = new ArrayList();
        fiatInvestments.add(fiatInvestment);

        when(investmentService.getInvestmentsByUserIdAndType(USER_ID, InvestmentType.CRYPTO))
                .thenReturn(cryptoInvestments);
        when(investmentService.getInvestmentsByUserIdAndType(USER_ID, InvestmentType.FIAT))
                .thenReturn(fiatInvestments);


        Collection<CurrencyInvestmentDTO> result = target.getUserCurrencyInvestments(USER_ID);


        verify(investmentService).getInvestmentsByUserIdAndType(USER_ID, InvestmentType.CRYPTO);
        verify(investmentService).getInvestmentsByUserIdAndType(USER_ID, InvestmentType.FIAT);
        Assertions.assertEquals(2, (long) result.size());
    }

    @Test
    void shouldGetCurrencyInvestmentsStats() {
        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();
        Asset fiatAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.FIAT).build();
        InvestmentTransaction tx = InvestmentTransaction.builder()
                .type(InvestmentTransactionType.BUY)
                .date(LocalDate.now())
                .quantity(BigDecimal.TEN).build();
        Investment cryptoInvestment = Investment.builder()
                .date(LocalDate.now())
                .asset(cryptoAsset)
                .symbol(CURRENCY_SYMBOL)
                .type(InvestmentType.CRYPTO)
                .updateType(InvestmentUpdateType.MANUAL)
                .transactions(Set.of(tx)).build();
        List cryptoInvestments = new ArrayList();
        cryptoInvestments.add(cryptoInvestment);
        Investment fiatInvestment = Investment.builder()
                .date(LocalDate.now())
                .asset(fiatAsset)
                .symbol(CURRENCY_SYMBOL)
                .type(InvestmentType.FIAT)
                .updateType(InvestmentUpdateType.MANUAL)
                .transactions(Set.of(tx)).build();
        List fiatInvestments = new ArrayList();
        fiatInvestments.add(fiatInvestment);

        when(investmentService.getInvestmentsByUserIdAndType(USER_ID, InvestmentType.CRYPTO))
                .thenReturn(cryptoInvestments);
        when(investmentService.getInvestmentsByUserIdAndType(USER_ID, InvestmentType.FIAT))
                .thenReturn(fiatInvestments);
        when(statisticsService.calculateTrend(anyCollection())).thenReturn(BigDecimal.TEN);
        when(statisticsService.calculateTotalChange(anyCollection())).thenReturn(BigDecimal.TEN);


        InvestmentStatsDTO result = target.getUserCurrencyInvestmentsStats(USER_ID);


        verify(investmentService).getInvestmentsByUserIdAndType(USER_ID, InvestmentType.CRYPTO);
        verify(investmentService).getInvestmentsByUserIdAndType(USER_ID, InvestmentType.FIAT);
        verify(statisticsService).calculateTrend(anyCollection());
        verify(statisticsService).calculateTotalChange(anyCollection());

        Assertions.assertEquals(BigDecimal.TEN, result.getTrend());
        Assertions.assertEquals(BigDecimal.TEN, result.getTotalChange());
    }
}
