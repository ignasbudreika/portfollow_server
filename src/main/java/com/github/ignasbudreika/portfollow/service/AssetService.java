package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.external.client.AlphaVantageClient;
import com.github.ignasbudreika.portfollow.external.dto.response.*;
import com.github.ignasbudreika.portfollow.model.Asset;
import com.github.ignasbudreika.portfollow.model.AssetHistory;
import com.github.ignasbudreika.portfollow.repository.AssetHistoryRepository;
import com.github.ignasbudreika.portfollow.repository.AssetRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Slf4j
@Service
@AllArgsConstructor
public class AssetService {
    private static final long PRICE_UPDATE_INTERVAL_IN_HOURS = 3L;
    private static final LocalDate PRICE_HISTORY_FETCH_SINCE = LocalDate.of(2022, 12, 1);

    private AssetRepository assetRepository;
    private AssetHistoryRepository assetHistoryRepository;
    private AlphaVantageClient alphaVantageClient;

    public Asset getAsset(String symbol, InvestmentType type) {
        return assetRepository.getBySymbolAndType(symbol, type);
    }

    @Transactional
    public Asset createAsset(String symbol, InvestmentType type) throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        BigDecimal price = fetchPrice(symbol, type);

        Asset asset = assetRepository.save(Asset.builder()
                .symbol(symbol)
                .type(type)
                .price(price).build());

        fetchPriceHistory(asset);

        return asset;
    }

    public BigDecimal fetchPrice(String symbol, InvestmentType type) throws IOException, URISyntaxException, BusinessLogicException, InterruptedException {
        switch (type) {
            case STOCK -> {
                StockDTO stock = null;
                try {
                    stock = alphaVantageClient.getStockData(symbol);
                } catch (Exception e) {
                    log.error("exception occured while fetching stock: {} data", symbol, e);
                    throw e;
                }

                return stock == null || stock.getPrice() == null ?
                        BigDecimal.ZERO : new BigDecimal(stock.getPrice());
            }
            case CRYPTO -> {
                CryptocurrencyDTO cryptocurrency = null;
                try {
                    cryptocurrency = alphaVantageClient.getCryptocurrencyData(symbol);
                } catch (Exception e) {
                    log.error("exception occured while fetching stock: {} data", symbol, e);
                    throw e;
                }

                return cryptocurrency == null || cryptocurrency.getExchangeRate() == null ?
                        BigDecimal.ZERO : new BigDecimal(cryptocurrency.getExchangeRate());
            }
            case FIAT -> {
                ForexDTO forex = null;
                try {
                    forex = alphaVantageClient.getCurrencyData(symbol);
                } catch (Exception e) {
                    log.error("exception occured while fetching stock: {} data", symbol, e);
                    throw e;
                }

                return forex == null || forex.getExchangeRate() == null ?
                        BigDecimal.ZERO : new BigDecimal(forex.getExchangeRate());
            }
            default -> {
                return BigDecimal.ZERO;
            }
        }
    }

    public void fetchPriceHistory(Asset asset) throws URISyntaxException, IOException, InterruptedException, BusinessLogicException {
        switch (asset.getType()) {
            case STOCK -> {
                try {
                    StockHistoryDailyDTO history = alphaVantageClient.getStockHistoryDaily(asset.getSymbol());

                    Collection<AssetHistory> assetHistory = history.getHistory().entrySet().stream()
                            .filter(entry -> LocalDate.parse(entry.getKey()).isAfter(PRICE_HISTORY_FETCH_SINCE))
                            .map(entry ->
                                    AssetHistory.builder()
                                            .asset(asset)
                                            .date(LocalDate.parse(entry.getKey()))
                                            .price(new BigDecimal(entry.getValue().getPrice()).setScale(8, RoundingMode.HALF_UP))
                                            .build()
                            ).toList();

                    assetHistoryRepository.saveAll(assetHistory);
                } catch (Exception e) {
                    log.error("exception occured while fetching stock: {} data", asset.getSymbol(), e);
                    throw e;
                }
            }
            case FIAT -> {
                try {
                    ForexHistoryDailyDTO history = alphaVantageClient.getForexHistoryDaily(asset.getSymbol());

                    Collection<AssetHistory> assetHistory = history.getHistory().entrySet().stream()
                            .filter(entry -> LocalDate.parse(entry.getKey()).isAfter(PRICE_HISTORY_FETCH_SINCE))
                            .map(entry ->
                                    AssetHistory.builder()
                                            .asset(asset)
                                            .date(LocalDate.parse(entry.getKey()))
                                            .price(new BigDecimal(entry.getValue().getPrice()).setScale(8, RoundingMode.HALF_UP))
                                            .build()
                            ).toList();

                    assetHistoryRepository.saveAll(assetHistory);
                } catch (Exception e) {
                    log.error("exception occured while fetching stock: {} data", asset.getSymbol(), e);
                    throw e;
                }
            }
            case CRYPTO -> {
                try {
                    CryptocurrencyHistoryDailyDTO history = alphaVantageClient.getCryptoHistoryDaily(asset.getSymbol());

                    Collection<AssetHistory> assetHistory = history.getHistory().entrySet().stream()
                            .filter(entry -> LocalDate.parse(entry.getKey()).isAfter(PRICE_HISTORY_FETCH_SINCE))
                            .map(entry ->
                                    AssetHistory.builder()
                                            .asset(asset)
                                            .date(LocalDate.parse(entry.getKey()))
                                            .price(new BigDecimal(entry.getValue().getPrice()).setScale(8, RoundingMode.HALF_UP))
                                            .build()
                            ).toList();

                    assetHistoryRepository.saveAll(assetHistory);
                } catch (Exception e) {
                    log.warn("exception occured while fetching cryptocurrency historical data", e);
                    throw e;
                }
            }
            default -> {
            }
        }
    }

    public void fetchPriceAndSaveInHistory(String symbol, InvestmentType type, LocalDate date) {
        Asset asset = assetRepository.getBySymbolAndType(symbol, type);
        if (asset == null) {
            log.warn("asset: {} of type: {} not found", symbol, type);
            return;
        }

        BigDecimal price = BigDecimal.ZERO;
        BigDecimal priceForHistory = BigDecimal.ZERO;

        switch (type) {
            case STOCK -> {
                StockDTO stock = new StockDTO();
                try {
                    stock = alphaVantageClient.getStockData(symbol);
                } catch (Exception e) {
                    log.error("exception occured while fetching stock: {} data", symbol, e);
                }

//                BigDecimal usdEur = getRecentPrice(USD, InvestmentType.FOREX);

                if (stock.getPrice() != null) {
                    price = new BigDecimal(stock.getPrice());
                }

                if (stock.getPreviousClose() != null) {
                    priceForHistory = new BigDecimal(stock.getPreviousClose());
                }
            }
            case CRYPTO -> {
                CryptocurrencyDTO cryptocurrency = new CryptocurrencyDTO();
                try {
                    cryptocurrency = alphaVantageClient.getCryptocurrencyData(symbol);
                } catch (Exception e) {
                    log.error("exception occured while fetching crypto: {} data", symbol, e);
                }

                if (cryptocurrency.getExchangeRate() != null) {
                    price = new BigDecimal(cryptocurrency.getExchangeRate());
                }
            }
            case FIAT -> {
                ForexDTO forex = new ForexDTO();
                try {
                    forex = alphaVantageClient.getCurrencyData(symbol);
                } catch (Exception e) {
                    log.error("exception occured while fetching forex: {} data", symbol, e);
                }

                if (forex.getExchangeRate() != null) {
                    price = new BigDecimal(forex.getExchangeRate());
                }
            }
            default -> {
            }
        }

        if (!price.equals(BigDecimal.ZERO)) {
            log.info("saving current asset: {} price: {}", asset.getId(), price);
            asset.setPrice(price);
            asset.setUpdatedAt(LocalDateTime.now());
            asset = assetRepository.save(asset);

            AssetHistory history = assetHistoryRepository.findByAssetIdAndDate(asset.getId(), date);
            if (history == null) {
                log.info("saving asset: {} price: {} for: {}", asset.getId(), price, date);
                assetHistoryRepository.save(AssetHistory.builder().asset(asset).date(date).price(price).build());
            } else {
                log.info("updating asset: {} price: {} for: {}", asset.getId(), price, date);
                history.setPrice(price);
                assetHistoryRepository.save(history);
            }
        }

        if (!priceForHistory.equals(BigDecimal.ZERO)) {
            AssetHistory history = assetHistoryRepository.findByAssetIdAndDate(asset.getId(), date.minusDays(1));
            if (history == null) {
                log.info("saving asset: {} price: {} for: {}", asset.getId(), priceForHistory, date.minusDays(1));
                assetHistoryRepository.save(AssetHistory.builder().asset(asset).date(date.minusDays(1)).price(priceForHistory).build());
            } else {
                log.info("updating asset: {} price: {} for: {}", asset.getId(), priceForHistory, date.minusDays(1));
                history.setPrice(priceForHistory);
                assetHistoryRepository.save(history);
            }
        }
    }

    public BigDecimal getLatestAssetPriceForDate(Asset asset, LocalDate date) {
        if (date.equals(LocalDate.now())) {
            return asset.getPrice();
        }

        AssetHistory history = assetHistoryRepository.findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(asset.getId(), date);
        if (history == null) {
            return asset.getPrice();
        }

        return history.getPrice();
    }

    public Iterable<Asset> getAllAssets() {
        return assetRepository.findAll();
    }
}
