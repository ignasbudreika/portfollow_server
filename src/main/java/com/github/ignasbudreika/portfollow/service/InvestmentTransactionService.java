package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.InvestmentTransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class InvestmentTransactionService {
    private InvestmentTransactionRepository transactionRepository;
    private InvestmentRepository investmentRepository;
    private PortfolioHistoryService portfolioHistoryService;

    public InvestmentTransaction createTransaction(Investment investment, BigDecimal quantity, InvestmentTransactionType type, LocalDate date) throws BusinessLogicException {
        log.info("creating: {} transaction for investment: {}", type, investment.getId());

        if (date.isBefore(LocalDate.of(2023, 1, 1))) {
            throw new BusinessLogicException("only transactions made since 2023-01-01 are supported");
        }

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

        if (tx.getInvestment().getUpdateType().equals(InvestmentUpdateType.SPECTROCOIN) || tx.getInvestment().getUpdateType().equals(InvestmentUpdateType.ALPACA)
                || tx.getInvestment().getUpdateType().equals(InvestmentUpdateType.ETHEREUM_WALLET)) {
            throw new BusinessLogicException("cannot delete connection investment tx");
        }

        if (tx.getType().equals(InvestmentTransactionType.BUY)
                && (tx.getInvestment().getLowestQuantitySince(tx.getDate()).compareTo(tx.getQuantity()) < 0)) {
            throw new BusinessLogicException(String.format("cannot delete BUY transaction: %s for investment: %s because the quantity after: %s would drop below 0",
                    txId, tx.getInvestment().getId(), tx.getDate()));
        }

        Investment investment = tx.getInvestment();
        investment.setTransactions(investment.getTransactions().stream().filter(transaction1 -> !transaction1.getId().equals(tx.getId())).collect(Collectors.toSet()));

        if (tx.getType().equals(InvestmentTransactionType.BUY)) {
            investment.setQuantity(investment.getQuantity().subtract(tx.getQuantity()));
        } else {
            investment.setQuantity(investment.getQuantity().add(tx.getQuantity()));
        }

        transactionRepository.deleteById(txId);
        investmentRepository.save(investment);

        portfolioHistoryService.updatePortfolioHistoryValue(user, tx.getDate());
    }
}
