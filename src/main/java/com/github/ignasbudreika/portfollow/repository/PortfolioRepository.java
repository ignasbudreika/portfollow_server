package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.Portfolio;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface PortfolioRepository extends CrudRepository<Portfolio, String> {
    Collection<Portfolio> findAllByUserIdOrderByDateAsc(String userId);
    Portfolio findFirstByDateBetweenOrderByDateAsc(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
