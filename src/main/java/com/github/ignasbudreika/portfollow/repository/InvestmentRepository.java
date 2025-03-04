package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType;
import com.github.ignasbudreika.portfollow.model.Investment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface InvestmentRepository extends CrudRepository<Investment, String> {
    Collection<Investment> findAllByUserIdAndType(String userId, InvestmentType type);
    Collection<Investment> findAllByUserId(String userId);
    Collection<Investment> findAllByUserIdAndUpdateTypeIn(String userId, List<InvestmentUpdateType> types);
    boolean existsByUserId(String userId);
    Investment findBySymbolAndConnectionId(String symbol, String connectionId);
    Collection<Investment> findAllByConnectionId(String connectionId);
}
