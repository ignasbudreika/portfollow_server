package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {
    private BigDecimal quantity;
    private InvestmentTransactionType type;
    private LocalDate date;
}
