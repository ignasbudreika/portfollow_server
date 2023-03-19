package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.model.Investment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface InvestmentRepository extends CrudRepository<Investment, String> {
    Collection<Investment> findAllByUserIdAndType(String userId, InvestmentType type);
    Collection<Investment> findAllByUserId(String userId);
}
