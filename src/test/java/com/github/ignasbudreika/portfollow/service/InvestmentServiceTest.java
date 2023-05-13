package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.CreateTransactionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import com.github.ignasbudreika.portfollow.external.client.AlphaVantageClient;
import com.github.ignasbudreika.portfollow.model.*;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import com.github.ignasbudreika.portfollow.repository.PortfolioHistoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;

import static com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType.*;
import static org.mockito.Mockito.*;

class InvestmentServiceTest {
    private static final String INVESTMENT_ID = "8d003914-3090-49be-8b68-a104bd388d51";
    private static final String USER_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final String USER_EMAIL = "test@e.mail";
    private static final String USER_USERNAME = "John Doe";
    private static final String ASSET_ID = "9f259780-8b4d-48ba-9c65-103ef1ea15a2";
    private static final BigDecimal ASSET_PRICE = BigDecimal.TEN;
    private static final String ASSET_SYMBOL = "AAPL";
    private static final BigDecimal QUANTITY = BigDecimal.ONE;
    private static final BigDecimal AMOUNT = BigDecimal.TEN;
    private static final String CONNECTION_ID = "43a29381-fd45-4fe7-8962-51973ca7ef9b";
    private final AssetService assetService = mock(AssetService.class);
    private final PortfolioHistoryService portfolioHistoryService = mock(PortfolioHistoryService.class);
    private final InvestmentTransactionService transactionService = mock(InvestmentTransactionService.class);
    private final InvestmentRepository investmentRepository = mock(InvestmentRepository.class);
    private final PortfolioHistoryRepository historyRepository = mock(PortfolioHistoryRepository.class);
    private final InvestmentService target = new InvestmentService(assetService,
                                                                   portfolioHistoryService,
                                                                   transactionService,
                                                                   investmentRepository,
                                                                   historyRepository);

    @Test
    void shouldReturnUserInvestments() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        when(investmentRepository.findAllByUserId(USER_ID)).thenReturn(List.of(Investment.builder()
                .transactions(new HashSet<>())
                .id(INVESTMENT_ID)
                .asset(cryptoAsset)
                .quantity(QUANTITY)
                .type(InvestmentType.CRYPTO).build()));


        Collection<InvestmentDTO> result = target.getUserInvestments(user);


        verify(investmentRepository).findAllByUserId(user.getId());

        Assertions.assertFalse(result.stream().findFirst().isEmpty());
        Assertions.assertEquals(INVESTMENT_ID, result.stream().findFirst().get().getId());
    }

    @Test
    void shouldReturnInvestmentsByUserId() {
        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        when(investmentRepository.findAllByUserId(USER_ID)).thenReturn(List.of(Investment.builder()
                .transactions(new HashSet<>())
                .id(INVESTMENT_ID)
                .asset(cryptoAsset)
                .quantity(QUANTITY)
                .type(InvestmentType.CRYPTO).build()));


        Collection<Investment> result = target.getInvestmentsByUserId(USER_ID);


        verify(investmentRepository).findAllByUserId(USER_ID);

        Assertions.assertFalse(result.stream().findFirst().isEmpty());
        Assertions.assertEquals(INVESTMENT_ID, result.stream().findFirst().get().getId());
    }

    @Test
    void shouldReturnUserInvestmentsByType() {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset stockAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.STOCK).build();

        when(investmentRepository.findAllByUserIdAndType(USER_ID, InvestmentType.STOCK)).thenReturn(List.of(
                Investment.builder()
                        .transactions(new HashSet<>())
                        .id(INVESTMENT_ID)
                        .asset(stockAsset)
                        .quantity(QUANTITY)
                        .type(InvestmentType.STOCK).build()
        ));


        Collection<InvestmentDTO> result = target.getUserInvestmentsByType(user, InvestmentType.STOCK);


        verify(investmentRepository).findAllByUserIdAndType(user.getId(), InvestmentType.STOCK);

        Assertions.assertFalse(result.stream().findFirst().isEmpty());
        Assertions.assertEquals(INVESTMENT_ID, result.stream().findFirst().get().getId());
        Assertions.assertEquals(InvestmentType.STOCK, result.stream().findFirst().get().getType());
    }

    @Test
    void shouldReturnInvestmentsByUserIdAndType() {
        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        when(investmentRepository.findAllByUserIdAndType(USER_ID, InvestmentType.STOCK)).thenReturn(List.of(Investment.builder()
                .transactions(new HashSet<>())
                .id(INVESTMENT_ID)
                .asset(cryptoAsset)
                .quantity(QUANTITY)
                .type(InvestmentType.CRYPTO).build()));


        Collection<Investment> result = target.getInvestmentsByUserIdAndType(USER_ID, InvestmentType.STOCK);


        verify(investmentRepository).findAllByUserIdAndType(USER_ID, InvestmentType.STOCK);

        Assertions.assertFalse(result.stream().findFirst().isEmpty());
        Assertions.assertEquals(INVESTMENT_ID, result.stream().findFirst().get().getId());
    }

    @Test
    void shouldCreateInvestmentTransaction_whenAssetExists() throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investment = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .updateType(MANUAL)
                .quantity(QUANTITY).build();

        when(assetService.getAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(cryptoAsset);
        when(investmentRepository.save(any())).thenReturn(investment);
        when(transactionService.createTransaction(investment, QUANTITY, InvestmentTransactionType.BUY, investment.getDate())).thenReturn(
                InvestmentTransaction.builder()
                        .type(InvestmentTransactionType.BUY)
                        .quantity(QUANTITY)
                        .date(investment.getDate()).build()
        );


        Investment result = target.createInvestment(investment, user);


        verify(assetService).getAsset(investment.getSymbol(), investment.getType());
        verify(investmentRepository).save(any());
        verify(transactionService).createTransaction(investment, QUANTITY, InvestmentTransactionType.BUY, investment.getDate());
        verify(portfolioHistoryService).createOrUpdatePortfolioHistory(any());

        Assertions.assertFalse(result.getTransactions().isEmpty());
    }

    @Test
    void shouldCreateInvestmentTransactionAndAsset_whenAssetDoesNotExist() throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investment = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .updateType(MANUAL)
                .quantity(QUANTITY).build();

        when(assetService.getAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(null);
        when(assetService.createAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(cryptoAsset);
        when(investmentRepository.save(any())).thenReturn(investment);
        when(transactionService.createTransaction(investment, QUANTITY, InvestmentTransactionType.BUY, investment.getDate())).thenReturn(
                InvestmentTransaction.builder()
                        .type(InvestmentTransactionType.BUY)
                        .quantity(QUANTITY)
                        .date(investment.getDate()).build()
        );


        Investment result = target.createInvestment(investment, user);


        verify(assetService).getAsset(investment.getSymbol(), investment.getType());
        verify(assetService).createAsset(investment.getSymbol(), investment.getType());
        verify(investmentRepository).save(any());
        verify(transactionService).createTransaction(investment, QUANTITY, InvestmentTransactionType.BUY, investment.getDate());
        verify(portfolioHistoryService).createOrUpdatePortfolioHistory(any());

        Assertions.assertFalse(result.getTransactions().isEmpty());
    }

    @Test
    void shouldThrowBusinessLogicException_whenAssetIsInvalid() throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Investment investment = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2023, 1, 1))
                .type(InvestmentType.CRYPTO)
                .quantity(QUANTITY).build();

        when(assetService.getAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(null);
        when(assetService.createAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(null);


        Assertions.assertThrows(BusinessLogicException.class, () -> target.createInvestment(investment, user));


        verify(assetService).getAsset(investment.getSymbol(), investment.getType());
        verify(assetService).createAsset(investment.getSymbol(), investment.getType());
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessLogicException_whenInvestmentDateIsBefore2023() throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investment = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2022, 12, 31))
                .type(InvestmentType.CRYPTO)
                .quantity(QUANTITY).build();

        when(assetService.getAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(null);
        when(assetService.createAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(cryptoAsset);


        Assertions.assertThrows(BusinessLogicException.class, () -> target.createInvestment(investment, user));


        verify(assetService).getAsset(investment.getSymbol(), investment.getType());
        verify(assetService).createAsset(investment.getSymbol(), investment.getType());
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void shouldCreateInvestment_whenInvestmentDoesNotExist() throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investment = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2022, 12, 31))
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .quantity(QUANTITY).build();


        when(assetService.getAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(null);
        when(assetService.createAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(cryptoAsset);
        when(investmentRepository.findBySymbolAndConnectionId(ASSET_SYMBOL, CONNECTION_ID)).thenReturn(null);
        when(investmentRepository.save(any())).thenReturn(investment);
        when(transactionService.createTransaction(investment, QUANTITY, InvestmentTransactionType.BUY, investment.getDate())).thenReturn(
                InvestmentTransaction.builder()
                        .type(InvestmentTransactionType.BUY)
                        .quantity(QUANTITY)
                        .date(investment.getDate()).build()
        );


        Investment result = target.saveInvestmentFetchedFromConnection(investment, CONNECTION_ID);


        verify(assetService).getAsset(investment.getSymbol(), investment.getType());
        verify(assetService).createAsset(investment.getSymbol(), investment.getType());
        verify(investmentRepository).findBySymbolAndConnectionId(ASSET_SYMBOL, CONNECTION_ID);
        verify(investmentRepository).save(any());
        verify(transactionService).createTransaction(investment, QUANTITY, InvestmentTransactionType.BUY, investment.getDate());
        verify(portfolioHistoryService).createOrUpdatePortfolioHistory(any());

        Assertions.assertFalse(result.getTransactions().isEmpty());
    }

    @Test
    void shouldReturnExistingInvestment_whenInvestmentExistsAndQuantityDoesNotDiffer() throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investment = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2022, 12, 31))
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .quantity(QUANTITY).build();

        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2022, 12, 31))
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(investment.getDate()).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(assetService.getAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(null);
        when(assetService.createAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(cryptoAsset);
        when(investmentRepository.findBySymbolAndConnectionId(ASSET_SYMBOL, CONNECTION_ID)).thenReturn(investmentWithTx);
        when(investmentRepository.save(any())).thenReturn(investment);


        Investment result = target.saveInvestmentFetchedFromConnection(investment, CONNECTION_ID);


        verify(assetService).getAsset(investment.getSymbol(), investment.getType());
        verify(assetService).createAsset(investment.getSymbol(), investment.getType());
        verify(investmentRepository).findBySymbolAndConnectionId(ASSET_SYMBOL, CONNECTION_ID);
        verify(investmentRepository, never()).save(any());

        Assertions.assertFalse(result.getTransactions().isEmpty());
    }

    @Test
    void shouldCreateBuyTransaction_whenInvestmentExistsAndBalanceIncreased() throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investment = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.now())
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .quantity(BigDecimal.valueOf(11)).build();

        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2022, 12, 31))
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(investment.getDate()).quantity(QUANTITY).build()))
                .quantity(QUANTITY).build();

        when(assetService.getAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(null);
        when(assetService.createAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(cryptoAsset);
        when(investmentRepository.findBySymbolAndConnectionId(ASSET_SYMBOL, CONNECTION_ID)).thenReturn(investmentWithTx);
        when(investmentRepository.save(any(Investment.class))).thenReturn(investmentWithTx);
        when(transactionService.createTransaction(investmentWithTx, BigDecimal.TEN, InvestmentTransactionType.BUY, investment.getDate())).thenReturn(
                InvestmentTransaction.builder()
                        .type(InvestmentTransactionType.BUY)
                        .quantity(BigDecimal.TEN)
                        .date(investment.getDate()).build()
        );


        Investment result = target.saveInvestmentFetchedFromConnection(investment, CONNECTION_ID);


        verify(assetService).getAsset(investment.getSymbol(), investment.getType());
        verify(assetService).createAsset(investment.getSymbol(), investment.getType());
        verify(investmentRepository).findBySymbolAndConnectionId(ASSET_SYMBOL, CONNECTION_ID);
        verify(investmentRepository).save(any());
        verify(transactionService).createTransaction(investmentWithTx, BigDecimal.TEN, InvestmentTransactionType.BUY, investment.getDate());
        verify(portfolioHistoryService).createOrUpdatePortfolioHistory(any());

        Assertions.assertFalse(result.getTransactions().isEmpty());
    }

    @Test
    void shouldCreateSellTransaction_whenInvestmentExistsAndBalanceDecreased() throws URISyntaxException, IOException, BusinessLogicException, InterruptedException {
        Asset cryptoAsset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        Investment investment = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.now())
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .quantity(BigDecimal.ONE).build();

        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .date(LocalDate.of(2022, 12, 31))
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(investment.getDate()).quantity(BigDecimal.TEN).build()))
                .quantity(QUANTITY).build();

        when(assetService.getAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(null);
        when(assetService.createAsset(ASSET_SYMBOL, InvestmentType.CRYPTO)).thenReturn(cryptoAsset);
        when(investmentRepository.findBySymbolAndConnectionId(ASSET_SYMBOL, CONNECTION_ID)).thenReturn(investmentWithTx);
        when(investmentRepository.save(any(Investment.class))).thenReturn(investmentWithTx);
        when(transactionService.createTransaction(investmentWithTx, BigDecimal.valueOf(9), InvestmentTransactionType.SELL, investment.getDate())).thenReturn(
                InvestmentTransaction.builder()
                        .type(InvestmentTransactionType.SELL)
                        .quantity(BigDecimal.valueOf(9))
                        .date(investment.getDate()).build()
        );


        Investment result = target.saveInvestmentFetchedFromConnection(investment, CONNECTION_ID);


        verify(assetService).getAsset(investment.getSymbol(), investment.getType());
        verify(assetService).createAsset(investment.getSymbol(), investment.getType());
        verify(investmentRepository).findBySymbolAndConnectionId(ASSET_SYMBOL, CONNECTION_ID);
        verify(investmentRepository).save(any());
        verify(transactionService).createTransaction(investmentWithTx, BigDecimal.valueOf(9), InvestmentTransactionType.SELL, investment.getDate());
        verify(portfolioHistoryService).createOrUpdatePortfolioHistory(any());

        Assertions.assertFalse(result.getTransactions().isEmpty());
    }

    @Test
    void shouldAddTransaction() throws UnauthorizedException, BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        CreateTransactionDTO tx = CreateTransactionDTO.builder()
                .date(LocalDate.now())
                .quantity(QUANTITY)
                .type(InvestmentTransactionType.BUY).build();

        Investment investmentWithTx = Investment.builder()
                .symbol(ASSET_SYMBOL)
                .user(user)
                .date(LocalDate.now())
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .updateType(MANUAL)
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.now()).quantity(BigDecimal.TEN).build()))
                .quantity(QUANTITY).build();

        when(investmentRepository.findById(INVESTMENT_ID)).thenReturn(Optional.of(investmentWithTx));
        when(transactionService.createTransaction(investmentWithTx, tx.getQuantity(), tx.getType(), tx.getDate())).thenReturn(
                InvestmentTransaction.builder()
                        .type(tx.getType())
                        .quantity(tx.getQuantity())
                        .date(tx.getDate()).build()
        );
        when(investmentRepository.save(any(Investment.class))).thenReturn(investmentWithTx);



        target.addTransaction(INVESTMENT_ID, tx, user);


        verify(investmentRepository).findById(INVESTMENT_ID);
        ArgumentCaptor<Investment> captor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository).save(captor.capture());
        Assertions.assertEquals(2, captor.getValue().getTransactions().size());
        verify(transactionService).createTransaction(investmentWithTx, tx.getQuantity(), tx.getType(), tx.getDate());
        verify(portfolioHistoryService).createOrUpdatePortfolioHistory(any());
    }

    @Test
    void shouldDeleteInvestment() throws UnauthorizedException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Investment investmentWithTx = Investment.builder()
                .id(INVESTMENT_ID)
                .symbol(ASSET_SYMBOL)
                .user(user)
                .date(LocalDate.now())
                .type(InvestmentType.CRYPTO)
                .connectionId(CONNECTION_ID)
                .portfolioHistories(Set.of(PortfolioHistory.builder().investments(List.of(Investment.builder().id(INVESTMENT_ID).build())).build()))
                .transactions(Set.of(InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.now()).quantity(BigDecimal.TEN).build()))
                .quantity(QUANTITY).build();

        when(investmentRepository.findById(INVESTMENT_ID)).thenReturn(Optional.of(investmentWithTx));
        when(historyRepository.findAllByInvestmentsId(INVESTMENT_ID))
                .thenReturn(Set.of(PortfolioHistory.builder().investments(List.of(Investment.builder().id(INVESTMENT_ID).build())).build()));


        target.deleteInvestment(INVESTMENT_ID, user);


        verify(investmentRepository).save(any(Investment.class));
        verify(historyRepository).save(any(PortfolioHistory.class));
        ArgumentCaptor<Investment> captor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository).delete(captor.capture());
        Assertions.assertEquals(INVESTMENT_ID, captor.getValue().getId());
        verify(portfolioHistoryService).updatePortfolioHistoryValue(user, investmentWithTx.getDate());
    }

    @Test
    void shouldStopPeriodicInvestment() throws UnauthorizedException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Investment periodicInvestment = Investment.builder()
                .id(INVESTMENT_ID)
                .user(user)
                .updateType(InvestmentUpdateType.DAILY).build();

        when(investmentRepository.findById(INVESTMENT_ID)).thenReturn(Optional.of(periodicInvestment));


        target.stopPeriodicInvestments(INVESTMENT_ID, user);


        ArgumentCaptor<Investment> captor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository).save(captor.capture());
        Assertions.assertEquals(INVESTMENT_ID, captor.getValue().getId());
        Assertions.assertEquals(MANUAL, captor.getValue().getUpdateType());
    }

    @Test
    void shouldDeleteConnection() {
        Investment connectionInvestment = Investment.builder()
                .id(INVESTMENT_ID)
                .connectionId(CONNECTION_ID)
                .updateType(InvestmentUpdateType.SPECTROCOIN).build();

        when(investmentRepository.findAllByConnectionId(CONNECTION_ID)).thenReturn(List.of(connectionInvestment));


        target.deleteConnection(CONNECTION_ID);


        ArgumentCaptor<Investment> captor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository).save(captor.capture());
        Assertions.assertEquals(INVESTMENT_ID, captor.getValue().getId());
        Assertions.assertEquals(MANUAL, captor.getValue().getUpdateType());
    }

    @ParameterizedTest
    @EnumSource(value = InvestmentUpdateType.class, mode = EnumSource.Mode.INCLUDE, names = {"DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY"})
    void shouldFetchPeriodicInvestments(InvestmentUpdateType type) throws BusinessLogicException {
        User user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username(USER_USERNAME).build();

        Asset asset = Asset.builder()
                .id(ASSET_ID)
                .price(ASSET_PRICE)
                .symbol(ASSET_SYMBOL)
                .type(InvestmentType.CRYPTO).build();

        LocalDate date = LocalDate.now();

        InvestmentTransaction tx = InvestmentTransaction.builder().type(InvestmentTransactionType.BUY).date(LocalDate.of(2023, 1, 1)).quantity(BigDecimal.TEN).build();
        Investment periodicInvestment = Investment.builder()
                .quantity(BigDecimal.TEN)
                .transactions(Set.of(tx))
                .id(INVESTMENT_ID)
                .amount(AMOUNT)
                .asset(asset)
                .user(user)
                .updateType(type).build();

        when(investmentRepository.findAllByUserIdAndUpdateTypeIn(eq(user.getId()), anyList())).thenReturn(List.of(periodicInvestment));

        when(transactionService.createTransaction(periodicInvestment, BigDecimal.TEN, InvestmentTransactionType.SELL, date)).thenReturn(
                InvestmentTransaction.builder()
                        .type(InvestmentTransactionType.BUY)
                        .quantity(BigDecimal.TEN)
                        .date(date).build()
        );


        target.fetchPeriodicInvestments(user, date);


        verify(investmentRepository).findAllByUserIdAndUpdateTypeIn(eq(user.getId()), anyList());
        LocalDate createSince = LocalDate.now();
        if (type.equals(DAILY)) {
            createSince = createSince.minusDays(1);
        } else if (type.equals(WEEKLY)) {
            createSince = createSince.minusWeeks(1);
        } else if (type.equals(MONTHLY)) {
            createSince = createSince.minusMonths(1);
        } else if (type.equals(QUARTERLY)) {
            createSince = createSince.minusMonths(3);
        } else if (type.equals(YEARLY)) {
            createSince = createSince.minusYears(1);
        }

        if (createSince.isAfter(tx.getDate())) {
            verify(transactionService).createTransaction(periodicInvestment, BigDecimal.ONE.setScale(8, RoundingMode.HALF_UP), InvestmentTransactionType.BUY, date);
            verify(investmentRepository).save(any(Investment.class));
        } else {
            verify(transactionService, never()).createTransaction(periodicInvestment, BigDecimal.ONE.setScale(8, RoundingMode.HALF_UP), InvestmentTransactionType.BUY, date);
            verify(investmentRepository, never()).save(any(Investment.class));
        }
    }
}
