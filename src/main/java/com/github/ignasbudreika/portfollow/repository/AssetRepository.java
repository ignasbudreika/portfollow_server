package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.Asset;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends CrudRepository<Asset, String> {
    Asset getBySymbolAndType(String symbol, InvestmentType type);
}
