package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.repository.AssetHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AssetHistoryService {
    private AssetHistoryRepository assetHistoryRepository;

    public boolean assetHistoryExists(String assetId) {
        return assetHistoryRepository.existsByAssetId(assetId);
    }
}
