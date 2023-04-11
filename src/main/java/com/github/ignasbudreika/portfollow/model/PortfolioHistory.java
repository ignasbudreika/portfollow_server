package com.github.ignasbudreika.portfollow.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "portfolio_history")
public class PortfolioHistory {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(precision = 19, scale = 8, nullable = false)
    private BigDecimal value = BigDecimal.ZERO;
    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @JoinTable(
            name = "portfolio_history_investment",
            joinColumns = @JoinColumn(name = "portfolio_history_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "investment_id", referencedColumnName = "id"))
    private Collection<Investment> investments = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name="portfolio_id")
    private Portfolio portfolio;
}
