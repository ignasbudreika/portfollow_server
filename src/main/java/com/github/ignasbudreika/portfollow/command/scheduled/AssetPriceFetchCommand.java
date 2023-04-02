package com.github.ignasbudreika.portfollow.command.scheduled;

import com.github.ignasbudreika.portfollow.service.AssetHistoryService;
import com.github.ignasbudreika.portfollow.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AssetPriceFetchCommand {
    @Autowired
    private AssetHistoryService assetHistoryService;
    @Autowired
    private AssetService assetService;

    @Scheduled(cron = "0 0 * * * *")
    public void updateAssetPrices() {
        assetService.getAllAssets().forEach(asset -> {
            log.info("updating asset: {} price", asset.getSymbol());
            if (!assetHistoryService.assetHistoryExists(asset.getId())) {
                log.info("creating asset: {} price history", asset.getSymbol());
                assetService.fetchPriceHistory(asset, asset.getType());
            }

            assetService.getRecentPrice(asset.getSymbol(), asset.getType());
        });
    }
}
