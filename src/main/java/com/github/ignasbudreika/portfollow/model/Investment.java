package com.github.ignasbudreika.portfollow.model;

import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.enums.InvestmentUpdateType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "investment")
public class Investment {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(nullable = false)
    private String symbol;
    @Column(precision = 19, scale = 8, nullable = false)
    private BigDecimal quantity;
    @Column(nullable = false)
    private InvestmentType type;
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
    @Column(name = "connection_id")
    private String connectionId;
    @ManyToOne
    @JoinColumn(name="asset_id")
    private Asset asset;
    private LocalDate date;
    @Column(name = "update_type", nullable = false)
    private InvestmentUpdateType updateType;
    @OneToMany(mappedBy = "investment", cascade = CascadeType.REMOVE)
    private Set<InvestmentTransaction> transactions = new HashSet<>();
    @ManyToMany(mappedBy = "investments")
    @Getter(AccessLevel.NONE)
    private Set<PortfolioHistory> portfolioHistories;

    public BigDecimal getQuantityAt(LocalDate date) {
        return getTransactions().stream().filter(transaction -> !transaction.getDate().isAfter(date)).map(transaction -> {
            if (transaction.getType().equals(InvestmentTransactionType.BUY)) {
                return transaction.getQuantity();
            } else {
                return transaction.getQuantity().negate();
            }
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getLowestQuantitySince(LocalDate date) {
        BigDecimal quantityAtProvidedDate = getTransactions().stream().filter(transaction -> !transaction.getDate().isAfter(date)).map(transaction -> {
            if (transaction.getType().equals(InvestmentTransactionType.BUY)) {
                return transaction.getQuantity();
            } else {
                return transaction.getQuantity().negate();
            }
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lowest = quantityAtProvidedDate;
        BigDecimal temporaryQuantity = quantityAtProvidedDate;

        Collection<BigDecimal> quantities = transactions.stream()
                .filter(transaction -> transaction.getDate().isAfter(date))
                .sorted(Comparator.comparing(InvestmentTransaction::getDate))
                .map(transaction -> {
                    if (transaction.getType().equals(InvestmentTransactionType.BUY)) {
                        return transaction.getQuantity();
                    } else {
                        return transaction.getQuantity().negate();
                    }
                }).collect(Collectors.toList());

        for(BigDecimal txQuantity: quantities) {
            temporaryQuantity = temporaryQuantity.add(txQuantity);
            if (txQuantity.compareTo(lowest) < 0) {
                lowest = temporaryQuantity;
            }
        }

        return lowest;
    }
}