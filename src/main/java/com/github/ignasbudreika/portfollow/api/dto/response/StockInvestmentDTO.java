package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockInvestmentDTO {
    private String id;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal value;
    @JsonProperty(value = "day_trend")
    private BigDecimal dayTrend;
    @JsonProperty(value = "total_change")
    private BigDecimal totalChange;
    private TransactionDTO[] transactions;
}
