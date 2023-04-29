package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.repository.AssetHistoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AssetHistoryServiceTest {
    private static final String ASSET_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";

    private final AssetHistoryRepository assetHistoryRepository = mock(AssetHistoryRepository.class);
    private final AssetHistoryService target = new AssetHistoryService(assetHistoryRepository);

    @Test
    void shouldReturnTrue_whenAssetHistoryExistsByAssetId() {
        when(assetHistoryRepository.existsByAssetId(ASSET_ID)).thenReturn(true);


        boolean result = target.assetHistoryExists(ASSET_ID);


        verify(assetHistoryRepository).existsByAssetId(ASSET_ID);

        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenAssetHistoryDoesNotExistByAssetId() {
        when(assetHistoryRepository.existsByAssetId(ASSET_ID)).thenReturn(false);


        boolean result = target.assetHistoryExists(ASSET_ID);


        verify(assetHistoryRepository).existsByAssetId(ASSET_ID);

        Assertions.assertFalse(result);
    }
}
