package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends CrudRepository<Portfolio, String> {
    Portfolio findByUserId(String userId);
    boolean existsByUserId(String userId);
    Page<Portfolio> findAllByPublished(boolean published, Pageable pageable);
}
