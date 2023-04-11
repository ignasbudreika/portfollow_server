package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.Portfolio;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends CrudRepository<Portfolio, String> {
    boolean existsByUserId(String userId);
}
