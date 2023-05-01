package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.external.client.AlphaVantageClient;
import com.github.ignasbudreika.portfollow.external.dto.response.*;
import com.github.ignasbudreika.portfollow.model.Asset;
import com.github.ignasbudreika.portfollow.model.AssetHistory;
import com.github.ignasbudreika.portfollow.repository.AssetHistoryRepository;
import com.github.ignasbudreika.portfollow.repository.AssetRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static org.mockito.Mockito.*;

class AssetServiceTest {
    private static final String ASSET_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final BigDecimal ASSET_PRICE = BigDecimal.TEN;
    private static final String ASSET_SYMBOL = "AAPL";
    private static final InvestmentType INVESTMENT_TYPE = InvestmentType.STOCK;
    private static final LocalDate DATE = LocalDate.of(2023, 1, 1);

    private final AssetRepository assetRepository = mock(AssetRepository.class);
    private final AssetHistoryRepository assetHistoryRepository = mock(AssetHistoryRepository.class);
    private final AlphaVantageClient alphaVantageClient = mock(AlphaVantageClient.class);
    private final AssetService target = new AssetService(assetRepository, assetHistoryRepository, alphaVantageClient);

    @Test
    void shouldGetAssetBySymbolAndType() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(BigDecimal.TEN)
                .symbol(ASSET_SYMBOL)
                .type(INVESTMENT_TYPE).build();

        when(assetRepository.getBySymbolAndType(ASSET_SYMBOL, INVESTMENT_TYPE)).thenReturn(asset);


        Asset result = target.getAsset(ASSET_SYMBOL, INVESTMENT_TYPE);


        verify(assetRepository).getBySymbolAndType(ASSET_SYMBOL, INVESTMENT_TYPE);

        Assertions.assertEquals(ASSET_ID, result.getId());
    }

    @Test
    void shouldReturnAssetPrice_whenDateIsEqualToToday() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(INVESTMENT_TYPE).build();


        BigDecimal result = target.getLatestAssetPriceForDate(asset, LocalDate.now());


        verify(assetHistoryRepository, never()).findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(eq(ASSET_ID), any(LocalDate.class));

        Assertions.assertEquals(ASSET_PRICE, result);
    }

    @Test
    void shouldReturnAssetPrice_whenAssetHistoryDoesNotExist() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(INVESTMENT_TYPE).build();


        BigDecimal result = target.getLatestAssetPriceForDate(asset, DATE);


        verify(assetHistoryRepository).findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(ASSET_ID, DATE);

        Assertions.assertEquals(ASSET_PRICE, result);
    }

    @Test
    void shouldReturnAssetPriceFromHistory_whenAssetHistoryBeforeDateExists() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(INVESTMENT_TYPE).build();
        AssetHistory assetHistory = AssetHistory.builder()
                .asset(asset)
                .date(DATE)
                .price(BigDecimal.valueOf(11)).build();

        when(assetHistoryRepository.findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(ASSET_ID, DATE)).thenReturn(assetHistory);


        BigDecimal result = target.getLatestAssetPriceForDate(asset, DATE);


        verify(assetHistoryRepository).findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(ASSET_ID, DATE);

        Assertions.assertEquals(assetHistory.getPrice(), result);
    }

    @Test
    void shouldReturnAllAssets() {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(INVESTMENT_TYPE).build();

        when(assetRepository.findAll()).thenReturn(List.of(asset));


        Iterable<Asset> result = target.getAllAssets();


        verify(assetRepository).findAll();

        Assertions.assertTrue(result.iterator().hasNext());
        Assertions.assertEquals(asset.getId(), result.iterator().next().getId());
    }

    @Test
    void shouldCreateAsset_whenAssetDoesNotExist() throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .symbol(ASSET_SYMBOL)
                .type(INVESTMENT_TYPE)
                .price(new BigDecimal("11")).build();

        when(assetRepository.getBySymbolAndType(ASSET_SYMBOL, INVESTMENT_TYPE)).thenReturn(null);
        when(alphaVantageClient.getStockData(ASSET_SYMBOL)).thenReturn(StockDTO.builder()
                .price("11").build());
        when(assetRepository.save(any())).thenReturn(asset);
        when(alphaVantageClient.getStockHistoryDaily(ASSET_SYMBOL)).thenReturn(StockHistoryDailyDTO.builder()
                .history(new HashMap<>()).build());


        Asset result = target.getOrCreateAsset(ASSET_SYMBOL, INVESTMENT_TYPE);


        verify(assetRepository).getBySymbolAndType(ASSET_SYMBOL, INVESTMENT_TYPE);
        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(captor.capture());
        Assertions.assertEquals(ASSET_SYMBOL, captor.getValue().getSymbol());
        Assertions.assertEquals(INVESTMENT_TYPE, captor.getValue().getType());
        Assertions.assertEquals(asset.getPrice(), captor.getValue().getPrice());
        verify(alphaVantageClient).getStockHistoryDaily(ASSET_SYMBOL);
        verify(assetHistoryRepository).saveAll(anyCollection());

        Assertions.assertEquals(ASSET_ID, result.getId());
    }

    @Test
    void shouldReturnAsset_whenAssetExists() throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .symbol(ASSET_SYMBOL)
                .type(INVESTMENT_TYPE)
                .price(ASSET_PRICE).build();

        when(assetRepository.getBySymbolAndType(ASSET_SYMBOL, INVESTMENT_TYPE)).thenReturn(asset);


        Asset result = target.getOrCreateAsset(ASSET_SYMBOL, INVESTMENT_TYPE);


        verify(assetRepository).getBySymbolAndType(ASSET_SYMBOL, INVESTMENT_TYPE);

        Assertions.assertEquals(ASSET_ID, result.getId());
    }

    @ParameterizedTest
    @EnumSource(value = InvestmentType.class)
    void shouldFetchPrice(InvestmentType type) throws IOException, URISyntaxException, BusinessLogicException, InterruptedException {
        if (type.equals(InvestmentType.STOCK)) {
            when(alphaVantageClient.getStockData(ASSET_SYMBOL)).thenReturn(StockDTO.builder()
                    .price("11").build());
        } else if (type.equals(InvestmentType.CRYPTO)) {
            when(alphaVantageClient.getCryptocurrencyData(ASSET_SYMBOL)).thenReturn(CryptocurrencyDTO.builder()
                    .exchangeRate("11").build());
        } else if (type.equals(InvestmentType.FIAT)) {
            when(alphaVantageClient.getCurrencyData(ASSET_SYMBOL)).thenReturn(ForexDTO.builder()
                    .exchangeRate("11").build());
        }


        BigDecimal price = target.fetchPrice(ASSET_SYMBOL, type);


        if (type.equals(InvestmentType.STOCK)) {
            verify(alphaVantageClient).getStockData(ASSET_SYMBOL);
        } else if (type.equals(InvestmentType.CRYPTO)) {
            verify(alphaVantageClient).getCryptocurrencyData(ASSET_SYMBOL);
        } else if (type.equals(InvestmentType.FIAT)) {
            verify(alphaVantageClient).getCurrencyData(ASSET_SYMBOL);
        }

        Assertions.assertEquals(new BigDecimal("11"), price);
    }

    @ParameterizedTest
    @EnumSource(value = InvestmentType.class)
    void shouldFetchPriceHistory(InvestmentType type) throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        String date = "2023-01-01";
        String price = "10";

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .symbol(ASSET_SYMBOL)
                .type(type)
                .price(ASSET_PRICE).build();

        if (type.equals(InvestmentType.STOCK)) {
            when(alphaVantageClient.getStockHistoryDaily(ASSET_SYMBOL)).thenReturn(StockHistoryDailyDTO.builder()
                    .history(Collections.singletonMap(
                            date, StockHistoryDailyDTO.HistoryDTO.builder().price(price).build()
                    )).build());
        } else if (type.equals(InvestmentType.CRYPTO)) {
            when(alphaVantageClient.getCryptoHistoryDaily(ASSET_SYMBOL)).thenReturn(CryptocurrencyHistoryDailyDTO.builder()
                    .history(Collections.singletonMap(
                            date, CryptocurrencyHistoryDailyDTO.HistoryDTO.builder().price(price).build()
                    )).build());
        } else if (type.equals(InvestmentType.FIAT)) {
            when(alphaVantageClient.getForexHistoryDaily(ASSET_SYMBOL)).thenReturn(ForexHistoryDailyDTO.builder()
                    .history(Collections.singletonMap(
                            date, ForexHistoryDailyDTO.HistoryDTO.builder().price(price).build()
                    )).build());
        }


        target.fetchPriceHistory(asset);


        if (type.equals(InvestmentType.STOCK)) {
            verify(alphaVantageClient).getStockHistoryDaily(ASSET_SYMBOL);
        } else if (type.equals(InvestmentType.CRYPTO)) {
            verify(alphaVantageClient).getCryptoHistoryDaily(ASSET_SYMBOL);
        } else if (type.equals(InvestmentType.FIAT)) {
            verify(alphaVantageClient).getForexHistoryDaily(ASSET_SYMBOL);
        }
        ArgumentCaptor<Iterable<AssetHistory>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(assetHistoryRepository).saveAll(captor.capture());
        Assertions.assertTrue(captor.getValue().iterator().hasNext());
        Assertions.assertEquals(new BigDecimal(price).setScale(8, RoundingMode.HALF_UP), captor.getValue().iterator().next().getPrice());
        Assertions.assertEquals(LocalDate.parse(date), captor.getValue().iterator().next().getDate());
    }

    @ParameterizedTest
    @EnumSource(value = InvestmentType.class)
    void shouldFetchPriceAndSaveInHistory(InvestmentType type) throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .symbol(ASSET_SYMBOL)
                .type(type)
                .price(ASSET_PRICE).build();
        when(assetRepository.getBySymbolAndType(ASSET_SYMBOL, type)).thenReturn(asset);

        when(assetRepository.save(any())).thenReturn(asset);
        if (type.equals(InvestmentType.STOCK)) {
            when(alphaVantageClient.getStockData(ASSET_SYMBOL)).thenReturn(StockDTO.builder()
                    .price("11").previousClose("12").build());
        } else if (type.equals(InvestmentType.CRYPTO)) {
            when(alphaVantageClient.getCryptocurrencyData(ASSET_SYMBOL)).thenReturn(CryptocurrencyDTO.builder()
                    .exchangeRate("11").build());
        } else if (type.equals(InvestmentType.FIAT)) {
            when(alphaVantageClient.getCurrencyData(ASSET_SYMBOL)).thenReturn(ForexDTO.builder()
                    .exchangeRate("11").build());
        }


        target.fetchPriceAndSaveInHistory(ASSET_SYMBOL, type, LocalDate.now());


        if (type.equals(InvestmentType.STOCK)) {
            verify(alphaVantageClient).getStockData(ASSET_SYMBOL);

            ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
            verify(assetRepository).save(captor.capture());
            Assertions.assertEquals(new BigDecimal("11"), captor.getValue().getPrice());

            verify(assetHistoryRepository, times(2)).save(any());
        } else if (type.equals(InvestmentType.CRYPTO)) {
            verify(alphaVantageClient).getCryptocurrencyData(ASSET_SYMBOL);

            ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
            verify(assetRepository).save(captor.capture());
            Assertions.assertEquals(new BigDecimal("11"), captor.getValue().getPrice());
        } else if (type.equals(InvestmentType.FIAT)) {
            verify(alphaVantageClient).getCurrencyData(ASSET_SYMBOL);

            ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
            verify(assetRepository).save(captor.capture());
            Assertions.assertEquals(new BigDecimal("11"), captor.getValue().getPrice());
        }
    }
}
