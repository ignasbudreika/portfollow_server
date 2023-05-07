package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.PortfolioHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface PortfolioHistoryRepository extends CrudRepository<PortfolioHistory, String> {
    Collection<PortfolioHistory> findAllByUserIdAndDateAfterOrderByDateAsc(String userId, LocalDate after);
    PortfolioHistory findFirstByUserIdAndDateBeforeOrderByDateDesc(String userId, LocalDate before);
    PortfolioHistory findFirstByUserIdAndDate(String userId, LocalDate date);
    PortfolioHistory findFirstByUserIdOrderByDateDesc(String userId);
    Collection<PortfolioHistory> findAllByInvestmentsId(String investmentId);
}
