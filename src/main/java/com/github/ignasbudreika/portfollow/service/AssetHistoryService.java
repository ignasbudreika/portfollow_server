package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.repository.AssetHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetHistoryService {
    @Autowired
    private AssetHistoryRepository assetHistoryRepository;

    public boolean assetHistoryExists(String assetId) {
        return assetHistoryRepository.existsByAssetId(assetId);
    }
}
