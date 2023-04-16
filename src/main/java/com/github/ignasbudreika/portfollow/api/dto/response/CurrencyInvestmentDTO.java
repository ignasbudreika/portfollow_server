package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrencyInvestmentDTO {
    private String id;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal value;
    private boolean crypto;
    private TransactionDTO[] transactions;
}
