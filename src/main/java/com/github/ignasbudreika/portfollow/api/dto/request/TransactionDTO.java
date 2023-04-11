package com.github.ignasbudreika.portfollow.api.dto.request;

import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDTO {
    private BigDecimal quantity;
    private InvestmentTransactionType type;
    private LocalDate date;
}
