package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.external.client.AlphaVantageClient;
import com.github.ignasbudreika.portfollow.external.dto.response.CryptocurrencyDTO;
import com.github.ignasbudreika.portfollow.external.dto.response.StockDTO;
import com.github.ignasbudreika.portfollow.model.Asset;
import com.github.ignasbudreika.portfollow.repository.AssetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class AssetService {
    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AlphaVantageClient alphaVantageClient;

    public BigDecimal getRecentPrice(String symbol, InvestmentType type) {
        Asset asset = assetRepository.getBySymbolAndType(symbol, type);
        // return saved price if it was fetched today
        if (asset != null
                && asset.getUpdatedAt().isAfter(LocalDateTime.now().toLocalDate().atStartOfDay())) {
            log.info("asset: {} was updated at: {}, returning last fetched price: {}",
                    asset.getSymbol(), asset.getUpdatedAt().truncatedTo(ChronoUnit.MINUTES), asset.getPrice());
            return asset.getPrice();
        }

        BigDecimal price = fetchPrice(symbol, type);

        if (asset == null && !BigDecimal.ZERO.equals(price)) {
            assetRepository.save(Asset.builder().symbol(symbol).type(type).price(price).build());
        } else if (asset != null && !BigDecimal.ZERO.equals(price)) {
            asset.setPrice(price);
            assetRepository.save(asset);
        }

        return price;
    }

    private BigDecimal fetchPrice(String symbol, InvestmentType type) {
        switch (type) {
            case STOCK -> {
                StockDTO stock = new StockDTO();
                try {
                    stock = alphaVantageClient.getStockData(symbol);
                } catch (Exception e) {
                }

                return stock.getPrice() == null ?
                        BigDecimal.ZERO : new BigDecimal(stock.getPrice());
            }
            case CRYPTOCURRENCY -> {
                CryptocurrencyDTO cryptocurrency = new CryptocurrencyDTO();
                try {
                    cryptocurrency = alphaVantageClient.getCryptocurrencyData(symbol);
                } catch (Exception e) {
                }

                return cryptocurrency.getExchangeRate() == null ?
                        BigDecimal.ZERO : new BigDecimal(cryptocurrency.getExchangeRate());
            }
            default -> {
                return BigDecimal.ZERO;
            }
        }
    }
}
