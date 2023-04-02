package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CryptocurrencyInvestmentDTO {
    private String id;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal value;
    private LocalDate date;
}
