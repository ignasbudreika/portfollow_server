package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.InvestmentTransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InvestmentTransactionServiceTest {
    private static final String INVESTMENT_ID = "8d003914-3090-49be-8b68-a104bd388d51";
    private static final String TRANSACTION_ID = "11113914-3090-49be-8b68-a104bd388d51";
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private static final String ASSET_SYMBOL = "AAPL";
    private static final BigDecimal QUANTITY = BigDecimal.ONE;
    private final InvestmentTransactionRepository transactionRepository = mock(InvestmentTransactionRepository.class);
    private final InvestmentRepository investmentRepository = mock(InvestmentRepository.class);
    private final PortfolioHistoryService portfolioHistoryService = mock(PortfolioHistoryService.class);
    private final InvestmentTransactionService target = new InvestmentTransactionService(transactionRepository, investmentRepository, portfolioHistoryService);

    @Test
    void shouldCreateTransaction() throws BusinessLogicException {
        Investment investmentWithTx = Investment.builder()
                .id(INVESTMENT_ID)
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        LocalDate date = LocalDate.now();

        InvestmentTransaction tx = InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).quantity(QUANTITY).date(date).build();

        when(transactionRepository.saveAndFlush(any(InvestmentTransaction.class))).thenReturn(tx);


        InvestmentTransaction result = target.createTransaction(investmentWithTx, QUANTITY, InvestmentTransactionType.BUY, date);


        verify(transactionRepository).saveAndFlush(any(InvestmentTransaction.class));

        Assertions.assertEquals(QUANTITY, result.getQuantity());
        Assertions.assertEquals(InvestmentTransactionType.BUY, result.getType());
        Assertions.assertEquals(date, result.getDate());
    }

    @Test
    void shouldThrowBusinessLogicException_whenTxDateIsBefore2023() {
        Investment investmentWithTx = Investment.builder()
                .id(INVESTMENT_ID)
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        LocalDate date = LocalDate.of(2022, 12, 31);


        Assertions.assertThrows(BusinessLogicException.class,  () -> target.createTransaction(investmentWithTx, QUANTITY, InvestmentTransactionType.BUY, date));


        verify(transactionRepository, never()).saveAndFlush(any(InvestmentTransaction.class));
    }

    @Test
    void shouldThrowBusinessLogicException_whenCreatingSellTxAndQuantityFallsBelowZero() {
        Investment investmentWithTx = Investment.builder()
                .id(INVESTMENT_ID)
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        LocalDate date = LocalDate.now();


        Assertions.assertThrows(BusinessLogicException.class,  () -> target.createTransaction(investmentWithTx, QUANTITY.add(BigDecimal.ONE), InvestmentTransactionType.SELL, date));


        verify(transactionRepository, never()).saveAndFlush(any(InvestmentTransaction.class));
    }

    @Test
    void shouldDeleteTransaction() throws UnauthorizedException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        InvestmentTransaction tx = InvestmentTransaction.builder()
                .id(TRANSACTION_ID)
                .type(InvestmentTransactionType.BUY)
                .date(LocalDate.of(2023, 1, 1))
                .quantity(QUANTITY).build();

        Investment investmentWithTx = Investment.builder()
                .id(INVESTMENT_ID)
                .symbol(ASSET_SYMBOL)
                .user(user)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(tx))
                .quantity(QUANTITY).build();

        tx.setInvestment(investmentWithTx);

        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(tx));


        target.deleteTransaction(TRANSACTION_ID, user);


        verify(transactionRepository).findById(TRANSACTION_ID);
        verify(transactionRepository).deleteById(TRANSACTION_ID);
        verify(investmentRepository).save(any(Investment.class));
        verify(portfolioHistoryService).updatePortfolioHistoryValue(user, tx.getDate());
    }

    @Test
    void shouldThrowBusinessLogicException_whenDeletingBuyTxAndQuantityFallsBelowZero() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        InvestmentTransaction buyTx = InvestmentTransaction.builder()
                .id(TRANSACTION_ID)
                .type(InvestmentTransactionType.BUY)
                .date(LocalDate.of(2023, 1, 1))
                .quantity(QUANTITY).build();

        InvestmentTransaction sellTx = InvestmentTransaction.builder()
                .id(TRANSACTION_ID + "_SELL")
                .type(InvestmentTransactionType.SELL)
                .quantity(QUANTITY)
                .date(LocalDate.now()).build();

        Investment investmentWithTx = Investment.builder()
                .id(INVESTMENT_ID)
                .symbol(ASSET_SYMBOL)
                .user(user)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .transactions(Set.of(buyTx, sellTx))
                .quantity(QUANTITY).build();

        buyTx.setInvestment(investmentWithTx);

        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(buyTx));


        Assertions.assertThrows(BusinessLogicException.class, () -> target.deleteTransaction(TRANSACTION_ID, user));


        verify(transactionRepository).findById(TRANSACTION_ID);
        verify(transactionRepository, never()).deleteById(TRANSACTION_ID);
    }
}
