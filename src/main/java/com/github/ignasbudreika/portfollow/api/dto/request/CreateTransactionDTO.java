package com.github.ignasbudreika.portfollow.api.dto.request;

import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CreateTransactionDTO {
    private BigDecimal quantity;
    private InvestmentTransactionType type;
    private LocalDate date;
}
