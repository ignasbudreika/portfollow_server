package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentTransactionRepository extends CrudRepository<InvestmentTransaction, String> {
}
