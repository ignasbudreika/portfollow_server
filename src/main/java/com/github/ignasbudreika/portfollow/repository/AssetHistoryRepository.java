package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.AssetHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetHistoryRepository extends CrudRepository<AssetHistory, String> {
}
