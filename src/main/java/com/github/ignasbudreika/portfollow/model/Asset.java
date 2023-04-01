package com.github.ignasbudreika.portfollow.model;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "symbol", "type" }) })
public class Asset {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(nullable = false)
    private String symbol;
    @Column(precision = 19, scale = 8, nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private InvestmentType type;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
    private Set<AssetHistory> history = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
    private Set<Investment> investments = new HashSet<>();
}
