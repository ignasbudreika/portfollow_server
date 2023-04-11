package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.request.TransactionDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.exception.BusinessLogicException;
import com.github.ignasbudreika.portfollow.exception.UnauthorizedException;
import com.github.ignasbudreika.portfollow.external.client.AlphaVantageClient;
import com.github.ignasbudreika.portfollow.model.Asset;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.InvestmentTransaction;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InvestmentService {
    @Autowired
    private AlphaVantageClient alphaVantageClient;

    @Autowired private AssetService assetService;
    @Autowired
    private PortfolioHistoryService portfolioHistoryService;
    @Autowired
    private InvestmentTransactionService transactionService;
    @Autowired
    private InvestmentRepository investmentRepository;

    public Collection<InvestmentDTO> getUserInvestments(User user) {
        Collection<Investment> investments = investmentRepository.findAllByUserId(user.getId());
        LocalDate date = LocalDate.now();

        Collection<InvestmentDTO> result = investments.stream().map(investment -> {
            BigDecimal price;
            if (investment.getAsset() != null) {
                price = investment.getAsset().getPrice();
            } else {
                price = assetService.getRecentPrice(investment.getSymbol(), investment.getType());
            }

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
            BigDecimal price;
            if (investment.getAsset() != null) {
                price = investment.getAsset().getPrice();
            } else {
                price = assetService.getRecentPrice(investment.getSymbol(), investment.getType());
            }

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

    public Investment createInvestment(Investment investment, User user) throws BusinessLogicException {
        Asset asset = assetService.getAsset(investment.getSymbol(), investment.getType());
        if (asset == null) {
            asset = assetService.createAsset(investment.getSymbol(), investment.getType());

            if (asset == null) {
                throw new BusinessLogicException("invalid symbol");
            }
        }

        if (investment.getDate().isBefore(LocalDate.of(2023, 1, 1))) {
            throw new BusinessLogicException("only investments made after 2023-01-01 are supported");
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

    public Investment saveInvestmentFetchedFromConnection(Investment investment, String connectionId) throws BusinessLogicException {
        Asset asset = assetService.getAsset(investment.getSymbol(), investment.getType());
        if (asset == null) {
            asset = assetService.createAsset(investment.getSymbol(), investment.getType());

            if (asset == null) {
                throw new BusinessLogicException("invalid symbol");
            }
        }

        Investment existing = investmentRepository.findBySymbolAndConnectionId(investment.getSymbol(), connectionId);
        if (existing != null) {
            // todo create tx for quantity diff
            log.info("investment: {} for connection: {} exists, updating quantity", investment.getSymbol(), connectionId);

            existing.setQuantity(investment.getQuantity());

            existing = investmentRepository.save(existing);

            portfolioHistoryService.createOrUpdatePortfolioHistory(existing);

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

    public Investment addTransaction(String investmentId, TransactionDTO transaction, User user) throws UnauthorizedException, BusinessLogicException {
        Investment investment = investmentRepository.findById(investmentId).orElseThrow(() -> new EntityNotFoundException());

        if (!investment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException();
        }

        InvestmentTransaction created = transactionService.createTransaction(investment, transaction.getQuantity(), transaction.getType(), transaction.getDate());
        Set<InvestmentTransaction> transactions = investment.getTransactions().stream().collect(Collectors.toSet());
        transactions.add(created);
        investment.setTransactions(transactions);

        portfolioHistoryService.createOrUpdatePortfolioHistory(investment);

        return investment;
    }
}
