package com.github.ignasbudreika.portfollow.model;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.math.BigDecimal;

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
}