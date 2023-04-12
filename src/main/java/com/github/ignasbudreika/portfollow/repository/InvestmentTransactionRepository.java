package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, String> {
}
