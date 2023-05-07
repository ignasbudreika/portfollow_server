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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class InvestmentService {
    private AlphaVantageClient alphaVantageClient;
    private AssetService assetService;
    private PortfolioHistoryService portfolioHistoryService;
    private InvestmentTransactionService transactionService;
    private InvestmentRepository investmentRepository;
    private PortfolioHistoryRepository historyRepository;

    public Collection<InvestmentDTO> getUserInvestments(User user) {
        Collection<Investment> investments = investmentRepository.findAllByUserId(user.getId());
        LocalDate date = LocalDate.now();

        Collection<InvestmentDTO> result = investments.stream().map(investment -> {
            BigDecimal price = investment.getAsset().getPrice();

            return InvestmentDTO.builder()
                    .id(investment.getId())
                    .symbol(investment.getSymbol())
                    .value(investment.getQuantityAt(date).multiply(price).setScale(8, RoundingMode.HALF_UP))
                    .type(investment.getType()).build();
        }).toList();

        return result;
    }

    public Collection<Investment> getInvestmentsByUserId(String userId) {
        return investmentRepository.findAllByUserId(userId);
    }

    public Collection<InvestmentDTO> getUserInvestmentsByType(User user, InvestmentType type) {
        Collection<Investment> investments = investmentRepository.findAllByUserIdAndType(user.getId(), type);
        LocalDate date = LocalDate.now();

        Collection<InvestmentDTO> result = investments.stream().map(investment -> {
            BigDecimal price = investment.getAsset().getPrice();

            return InvestmentDTO.builder()
                    .id(investment.getId())
                    .symbol(investment.getSymbol())
                    .value(investment.getQuantityAt(date).multiply(price).setScale(8, RoundingMode.HALF_UP))
                    .type(investment.getType()).build();
        }).toList();

        return result;
    }

    public Collection<Investment> getInvestmentsByUserIdAndType(String userId, InvestmentType type) {
        return investmentRepository.findAllByUserIdAndType(userId, type);
    }

    public Investment createInvestment(Investment investment, User user) throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        Asset asset = assetService.getAsset(investment.getSymbol(), investment.getType());
        if (asset == null) {
            asset = assetService.getOrCreateAsset(investment.getSymbol(), investment.getType());

            if (asset == null) {
                throw new BusinessLogicException("invalid symbol");
            }
        }

        if (investment.getDate().isBefore(LocalDate.of(2023, 1, 1))) {
            throw new BusinessLogicException("only investments made since 2023-01-01 are supported");
        }

        if (investment.getUpdateType().equals(InvestmentUpdateType.DAILY) || investment.getUpdateType().equals(InvestmentUpdateType.WEEKLY)
         || investment.getUpdateType().equals(InvestmentUpdateType.MONTHLY) || investment.getUpdateType().equals(InvestmentUpdateType.QUARTERLY)
         || investment.getUpdateType().equals(InvestmentUpdateType.YEARLY)) {
            investment.setQuantity(investment.getAmount().divide(asset.getPrice(), 8, RoundingMode.HALF_UP));
        }

        investment.setUser(user);
        investment.setAsset(asset);
        investment = investmentRepository.save(investment);

        InvestmentTransaction transaction = transactionService.createTransaction(investment, investment.getQuantity(), InvestmentTransactionType.BUY, investment.getDate());
        Set<InvestmentTransaction> transactions = new HashSet<>();
        transactions.add(transaction);
        investment.setTransactions(transactions);

        portfolioHistoryService.createOrUpdatePortfolioHistory(investment);

        return investment;
    }

    @Transactional
    public Investment saveInvestmentFetchedFromConnection(Investment investment, String connectionId) throws BusinessLogicException, URISyntaxException, IOException, InterruptedException {
        Asset asset = assetService.getAsset(investment.getSymbol(), investment.getType());
        if (asset == null) {
            asset = assetService.getOrCreateAsset(investment.getSymbol(), investment.getType());

            if (asset == null) {
                throw new BusinessLogicException("invalid symbol");
            }
        }

        Investment existing = investmentRepository.findBySymbolAndConnectionId(investment.getSymbol(), connectionId);
        if (existing != null) {
            if (existing.getQuantityAt(LocalDate.now()).compareTo(investment.getQuantity()) != 0) {
                InvestmentTransaction transaction;

                BigDecimal quantityDiff = existing.getQuantityAt(LocalDate.now()).subtract(investment.getQuantity());
                if (quantityDiff.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("investment: {} for connection: {} exists, quantity has decreased by: {}, creating SELL tx",
                            investment.getSymbol(), connectionId, quantityDiff);

                    transaction = transactionService.createTransaction(existing, quantityDiff.abs(), InvestmentTransactionType.SELL, investment.getDate());
                } else {
                    log.info("investment: {} for connection: {} exists, quantity has increased by: {}, creating BUY tx",
                            investment.getSymbol(), connectionId, quantityDiff.abs());

                    transaction = transactionService.createTransaction(existing, quantityDiff.abs(), InvestmentTransactionType.BUY, investment.getDate());
                }

                Set<InvestmentTransaction> transactions = existing.getTransactions().stream().collect(Collectors.toSet());
                transactions.add(transaction);
                existing.setTransactions(transactions);

                existing.setQuantity(investment.getQuantity());

                existing = investmentRepository.save(existing);

                portfolioHistoryService.createOrUpdatePortfolioHistory(existing);
            } else {
                log.info("investment: {} for connection: {} exists, but quantity does not differ", investment.getSymbol(), connectionId);
            }

            return existing;
        }

        investment.setConnectionId(connectionId);
        investment.setAsset(asset);
        investment.setDate(LocalDate.now());

        investment = investmentRepository.save(investment);

        InvestmentTransaction transaction = transactionService.createTransaction(investment, investment.getQuantity(), InvestmentTransactionType.BUY, investment.getDate());
        Set<InvestmentTransaction> transactions = new HashSet<>();
        transactions.add(transaction);
        investment.setTransactions(transactions);

        portfolioHistoryService.createOrUpdatePortfolioHistory(investment);

        return investment;
    }

    public Investment addTransaction(String investmentId, CreateTransactionDTO transaction, User user) throws UnauthorizedException, BusinessLogicException {
        Investment investment = investmentRepository.findById(investmentId).orElseThrow(EntityNotFoundException::new);

        if (!investment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException();
        }

        if (investment.getUpdateType().equals(InvestmentUpdateType.SPECTROCOIN) || investment.getUpdateType().equals(InvestmentUpdateType.ALPACA)
            || investment.getUpdateType().equals(InvestmentUpdateType.ETHEREUM_WALLET)) {
            throw new BusinessLogicException("cannot add tx for connection investments");
        }

        InvestmentTransaction created = transactionService.createTransaction(investment, transaction.getQuantity(), transaction.getType(), transaction.getDate());
        Set<InvestmentTransaction> transactions = investment.getTransactions().stream().collect(Collectors.toSet());
        transactions.add(created);
        investment.setTransactions(transactions);

        if (transaction.getType().equals(InvestmentTransactionType.BUY)) {
            investment.setQuantity(investment.getQuantity().add(transaction.getQuantity()));
        } else {
            investment.setQuantity(investment.getQuantity().subtract(transaction.getQuantity()));
        }

        if (transaction.getDate().isBefore(investment.getDate()) || transactions.size() == 1) {
            investment.setDate(transaction.getDate());
        }
        investment = investmentRepository.save(investment);

        portfolioHistoryService.createOrUpdatePortfolioHistory(investment);

        return investment;
    }

    public void deleteInvestment(String investmentId, User user) throws UnauthorizedException {
        log.info("deleting investment: {}", investmentId);

        Optional<Investment> investment = investmentRepository.findById(investmentId);
        if (investment.isEmpty()) {
            return;
        }

        Investment inv = investment.get();

        if (!inv.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException();
        }

        inv.setPortfolioHistories(new HashSet<>());
        investmentRepository.save(inv);

        Collection<PortfolioHistory> histories = historyRepository.findAllByInvestmentsId(investmentId);
        histories.forEach(history -> {
            history.setInvestments(history.getInvestments().stream().filter(investment1 -> !investment1.getId().equals(investmentId)).collect(Collectors.toList()));
            historyRepository.save(history);
        });

        investmentRepository.delete(inv);

        portfolioHistoryService.updatePortfolioHistoryValue(user, inv.getDate());
    }

    public void stopPeriodicInvestments(String id, User user) throws UnauthorizedException {
        Optional<Investment> investment = investmentRepository.findById(id);
        if (investment.isEmpty()) {
            return;
        }

        Investment inv = investment.get();
        if (!inv.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException();
        }

        if (inv.getUpdateType().equals(InvestmentUpdateType.MANUAL)
                || inv.getUpdateType().equals(InvestmentUpdateType.SPECTROCOIN)
                || inv.getUpdateType().equals(InvestmentUpdateType.ETHEREUM_WALLET)
                || inv.getUpdateType().equals(InvestmentUpdateType.ALPACA)) {
            return;
        }

        inv.setUpdateType(InvestmentUpdateType.MANUAL);

        investmentRepository.save(inv);
    }

    public void deleteConnection(String connectionId) {
        investmentRepository.findAllByConnectionId(connectionId)
                .stream().forEach(investment -> {
                    investment.setConnectionId(null);
                    investment.setUpdateType(InvestmentUpdateType.MANUAL);
                    investmentRepository.save(investment);
                });
    }

    @Transactional
    public void fetchPeriodicInvestments(User user, LocalDate date) {
        investmentRepository.findAllByUserId(user.getId()).stream().filter(investment ->
                investment.getUpdateType() != InvestmentUpdateType.MANUAL
                        && investment.getUpdateType() != InvestmentUpdateType.SPECTROCOIN
                        && investment.getUpdateType() != InvestmentUpdateType.ETHEREUM_WALLET
                        && investment.getUpdateType() != InvestmentUpdateType.ALPACA).forEach(investment -> {
                            InvestmentTransaction lastTx = investment.getTransactions().stream()
                                    .sorted(Comparator.comparing(InvestmentTransaction::getDate).reversed())
                                    .findFirst().orElse(null);
                            try {
                                if (lastTx == null) {
                                        createPeriodicTransaction(investment, date);
                                } else {
                                    switch (investment.getUpdateType()) {
                                        case DAILY -> {
                                            if (!lastTx.getDate().plusDays(1).isAfter(date)) {
                                                createPeriodicTransaction(investment, date);
                                            }
                                        }
                                        case WEEKLY -> {
                                            if (!lastTx.getDate().plusWeeks(1).isAfter(date)) {
                                                createPeriodicTransaction(investment, date);
                                            }
                                        }
                                        case MONTHLY -> {
                                            if (!lastTx.getDate().plusMonths(1).isAfter(date)) {
                                                createPeriodicTransaction(investment, date);
                                            }
                                        }
                                        case QUARTERLY -> {
                                            if (!lastTx.getDate().plusMonths(3).isAfter(date)) {
                                                createPeriodicTransaction(investment, date);
                                            }
                                        }
                                        case YEARLY -> {
                                            if (!lastTx.getDate().plusYears(1).isAfter(date)) {
                                                createPeriodicTransaction(investment, date);
                                            }
                                        }
                                        default -> {
                                            log.info("not a periodic investment: {}, skipping transaction creation", investment.getId());
                                        }
                                    }
                                }
                            } catch (BusinessLogicException e) {
                                log.error("failed to create periodic transaction for investment: {}", investment.getId(), e);
                            }
        });
    }

    private void createPeriodicTransaction(Investment investment, LocalDate date) throws BusinessLogicException {
        BigDecimal quantity = investment.getAmount().divide(investment.getAsset().getPrice(), 8, RoundingMode.HALF_UP);

        InvestmentTransaction created = transactionService.createTransaction(investment, quantity, InvestmentTransactionType.BUY, date);
        Set<InvestmentTransaction> transactions = investment.getTransactions().stream().collect(Collectors.toSet());
        transactions.add(created);
        investment.setTransactions(transactions);

        investment.setQuantity(investment.getQuantity().add(quantity));

        investment = investmentRepository.save(investment);

        portfolioHistoryService.createOrUpdatePortfolioHistory(investment);
    }
}
