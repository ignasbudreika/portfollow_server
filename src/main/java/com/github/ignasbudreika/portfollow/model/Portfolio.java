package com.github.ignasbudreika.portfollow.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
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
    @JoinTable(
            name = "portfolio_investment",
            joinColumns = @JoinColumn(name = "portfolio_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "investment_id", referencedColumnName = "id"))
    @Getter(AccessLevel.NONE)
    private Collection<Investment> investments;
}
