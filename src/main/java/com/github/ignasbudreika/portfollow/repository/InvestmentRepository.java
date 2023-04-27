package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, String> {
    Collection<Investment> findAllByUserIdAndType(String userId, InvestmentType type);
    Collection<Investment> findAllByUserId(String userId);
    boolean existsByUserId(String userId);
    Investment findBySymbolAndConnectionId(String symbol, String connectionId);
    Collection<Investment> findAllByConnectionId(String connectionId);
    Collection<Investment> findByPortfolioHistoriesId(String portfolioId);
    Collection<Investment> findByTypeAndPortfolioHistoriesId(InvestmentType type, String portfolioId);
}
