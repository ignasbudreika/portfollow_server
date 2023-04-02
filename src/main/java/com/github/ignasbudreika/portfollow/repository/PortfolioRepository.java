package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.Portfolio;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface PortfolioRepository extends CrudRepository<Portfolio, String> {
    Collection<Portfolio> findAllByUserIdAndDateAfterOrderByDateAsc(String userId, LocalDate after);
    Portfolio findFirstByUserIdAndDateBeforeOrderByDateDesc(String userId, LocalDate before);
    Portfolio findFirstByUserIdAndDate(String userId, LocalDate date);
    Portfolio findFirstByUserIdOrderByDateDesc(String userId);
}
