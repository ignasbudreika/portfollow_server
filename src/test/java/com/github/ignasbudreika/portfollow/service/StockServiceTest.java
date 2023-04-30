package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.StockDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentStatsDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.StockInvestmentDTO;
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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class StockServiceTest {
    private static final String ASSET_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final BigDecimal ASSET_PRICE = BigDecimal.TEN;
    private static final String ASSET_SYMBOL = "AAPL";
    private static final String CURRENCY_SYMBOL = "EUR";
    private static final BigDecimal CURRENCY_QUANTITY = BigDecimal.TEN;
    private static final String USER_ID = "56d1dd5d-ebb4-45da-84d3-92f7e21abe08";
    private final StatisticsService statisticsService = mock(StatisticsService.class);
    private final InvestmentService investmentService = mock(InvestmentService.class);
    private final StockService target = new StockService(statisticsService, investmentService);

    @Test
    void shouldCreateStockInvestment() throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        User user = User.builder().id(USER_ID).build();
        StockDTO stock = StockDTO.builder()
                .date(LocalDate.now())
                .quantity(CURRENCY_QUANTITY)
                .ticker(ASSET_SYMBOL).build();
        Investment investment = Investment.builder()
                .date(stock.getDate())
                .quantity(stock.getQuantity())
                .symbol(stock.getTicker()).build();

        when(investmentService.createInvestment(any(), eq(user))).thenReturn(investment);


        StockInvestmentDTO result = target.createStockInvestment(stock, user);


        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(investmentService).createInvestment(any(), captor.capture());
        Assertions.assertEquals(user.getId(), captor.getValue().getId());

        Assertions.assertEquals(investment.getSymbol(), result.getTicker());
    }

    @Test
    void shouldGetStockInvestments() {
        Asset stockAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.STOCK).build();
        InvestmentTransaction tx = InvestmentTransaction.builder()
                .type(InvestmentTransactionType.BUY)
                .date(LocalDate.now())
                .quantity(BigDecimal.TEN).build();
        Investment stockInvestment = Investment.builder()
                .date(LocalDate.now())
                .asset(stockAsset)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO)
                .updateType(InvestmentUpdateType.MANUAL)
                .transactions(Set.of(tx)).build();

        when(investmentService.getInvestmentsByUserIdAndType(USER_ID, InvestmentType.STOCK))
                .thenReturn(List.of(stockInvestment));


        Collection<StockInvestmentDTO> result = target.getUserStockInvestments(USER_ID);


        verify(investmentService).getInvestmentsByUserIdAndType(USER_ID, InvestmentType.STOCK);
        Assertions.assertEquals(1, (long) result.size());
    }

    @Test
    void shouldGetCurrencyInvestmentsStats() {
        Asset stockAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.STOCK).build();
        InvestmentTransaction tx = InvestmentTransaction.builder()
                .type(InvestmentTransactionType.BUY)
                .date(LocalDate.now())
                .quantity(BigDecimal.TEN).build();
        Investment stockInvestment = Investment.builder()
                .date(LocalDate.now())
                .asset(stockAsset)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO)
                .updateType(InvestmentUpdateType.MANUAL)
                .transactions(Set.of(tx)).build();

        when(investmentService.getInvestmentsByUserIdAndType(USER_ID, InvestmentType.STOCK))
                .thenReturn(List.of(stockInvestment));
        when(statisticsService.calculateTrend(anyCollection())).thenReturn(BigDecimal.TEN);
        when(statisticsService.calculateTotalChange(anyCollection())).thenReturn(BigDecimal.TEN);


        InvestmentStatsDTO result = target.getUserStockInvestmentsStats(USER_ID);


        verify(investmentService).getInvestmentsByUserIdAndType(USER_ID, InvestmentType.STOCK);
        verify(statisticsService).calculateTrend(anyCollection());
        verify(statisticsService).calculateTotalChange(anyCollection());

        Assertions.assertEquals(BigDecimal.TEN, result.getTrend());
        Assertions.assertEquals(BigDecimal.TEN, result.getTotalChange());
    }
}
