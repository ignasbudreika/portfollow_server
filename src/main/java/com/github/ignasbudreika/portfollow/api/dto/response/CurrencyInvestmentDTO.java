package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(value = "day_trend")
    private BigDecimal dayTrend;
    @JsonProperty(value = "total_change")
    private BigDecimal totalChange;
    @JsonProperty(value = "update_type")
    private String updateType;
    private TransactionDTO[] transactions;
}
