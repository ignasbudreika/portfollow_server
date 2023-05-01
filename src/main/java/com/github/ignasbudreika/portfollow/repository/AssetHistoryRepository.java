package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.AssetHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AssetHistoryRepository extends CrudRepository<AssetHistory, String> {
    AssetHistory findFirstByAssetIdAndDateLessThanEqualOrderByDateDesc(String id, LocalDate date);
    AssetHistory findByAssetIdAndDate(String id, LocalDate date);
    boolean existsByAssetId(String id);
    boolean existsByAssetIdAndDate(String id, LocalDate date);
}
