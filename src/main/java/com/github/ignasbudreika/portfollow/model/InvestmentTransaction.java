package com.github.ignasbudreika.portfollow.model;

import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "investment_transaction")
public class InvestmentTransaction {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private InvestmentTransactionType type;
    @Column(precision = 19, scale = 8, nullable = false)
    private BigDecimal quantity;
    @ManyToOne
    @JoinColumn(name="investment_id", nullable=false)
    @EqualsAndHashCode.Exclude
    private Investment investment;
    private LocalDate date;
}
