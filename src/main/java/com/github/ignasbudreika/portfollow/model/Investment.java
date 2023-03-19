package com.github.ignasbudreika.portfollow.model;

import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
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
    
    private String symbol;
    private BigDecimal value;
    private BigDecimal quantity;
    private InvestmentType type;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
}
