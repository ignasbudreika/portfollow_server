package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.InvestmentTransactionRepository;
import com.github.ignasbudreika.portfollow.repository.PortfolioHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InvestmentTransactionService {
    @Autowired
    private InvestmentTransactionRepository transactionRepository;
    @Autowired
    private InvestmentRepository investmentRepository;
    @Autowired
    private PortfolioHistoryService portfolioHistoryService;
    @Autowired
    private PortfolioHistoryRepository portfolioHistoryRepository;

    public InvestmentTransaction createTransaction(Investment investment, BigDecimal quantity, InvestmentTransactionType type, LocalDate date) throws BusinessLogicException {
        log.info("creating: {} transaction for investment: {}", type, investment.getId());

        if (date.isBefore(LocalDate.of(2023, 1, 1)) || date.isAfter(LocalDate.now())) {
            throw new BusinessLogicException("only investments made since 2023-01-01 are supported");
        }

        if (type.equals(InvestmentTransactionType.SELL) && (investment.getLowestQuantitySince(date).compareTo(quantity) < 0)) {
            throw new BusinessLogicException(String.format("cannot create sell transaction for investment: %s because the quantity after: %s would drop below 0",
                    investment.getId(), date));
        }

        return transactionRepository.saveAndFlush(InvestmentTransaction.builder()
                .investment(investment)
                .quantity(quantity)
                .type(type)
                .date(date).build());
    }

    public void deleteTransaction(String txId, User user) throws UnauthorizedException, BusinessLogicException {
        log.info("deleting tx: {}", txId);

        Optional<InvestmentTransaction> transaction = transactionRepository.findById(txId);
        if (transaction.isEmpty()) {
            return;
        }

        InvestmentTransaction tx = transaction.get();

        if (!tx.getInvestment().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException();
        }

        if (tx.getType().equals(InvestmentTransactionType.BUY)
                && (tx.getInvestment().getLowestQuantitySince(tx.getDate()).compareTo(tx.getQuantity()) < 0)) {
            throw new BusinessLogicException(String.format("cannot delete BUY transaction: %s for investment: %s because the quantity after: %s would drop below 0",
                    txId, tx.getInvestment().getId(), tx.getDate()));
        }

        Investment investment = tx.getInvestment();
        investment.setTransactions(investment.getTransactions().stream().filter(transaction1 -> !transaction1.getId().equals(tx.getId())).collect(Collectors.toSet()));

        transactionRepository.deleteById(txId);
        investmentRepository.save(investment);

        portfolioHistoryService.updatePortfolioHistoryValue(user, tx.getDate());
    }
}
