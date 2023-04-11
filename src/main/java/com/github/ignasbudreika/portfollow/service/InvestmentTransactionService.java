package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.InvestmentTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
public class InvestmentTransactionService {
    @Autowired
    private InvestmentTransactionRepository transactionRepository;
    @Autowired
    private InvestmentRepository investmentRepository;

    public InvestmentTransaction createTransaction(Investment investment, BigDecimal quantity, InvestmentTransactionType type, LocalDate date) throws BusinessLogicException {
        log.info("creating: {} transaction for investment: {}", type, investment.getId());

        if (type.equals(InvestmentTransactionType.SELL) && (investment.getLowestQuantitySince(date).compareTo(quantity) < 0)) {
            throw new BusinessLogicException(String.format("cannot create sell transaction for investment: %s because the quantity after: %s would drop below 0",
                    investment.getId(), date));
        }

        return transactionRepository.save(InvestmentTransaction.builder()
                .investment(investment)
                .quantity(quantity)
                .type(type)
                .date(date).build());
    }
}
