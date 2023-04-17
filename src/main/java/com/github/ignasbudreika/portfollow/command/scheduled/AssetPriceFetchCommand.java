package com.github.ignasbudreika.portfollow.command.scheduled;

import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.service.AssetHistoryService;
import com.github.ignasbudreika.portfollow.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
            try {
                log.info("updating asset: {} price", asset.getSymbol());
                if (!assetHistoryService.assetHistoryExists(asset.getId())) {
                    log.info("creating asset: {} price history", asset.getSymbol());
                        assetService.fetchPriceHistory(asset, asset.getType());
                }

                assetService.fetchPriceAndSaveInHistory(asset.getSymbol(), asset.getType(), LocalDate.now());
            } catch (BusinessLogicException e) {
                log.warn("unable to fetch price history for asset: {}", asset.getId());
            } catch (Exception e) {
                log.error("unable to fetch price history for asset: {}", asset.getId());
            }
        });
    }
}
