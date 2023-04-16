package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvestmentStatsDTO {
    @JsonProperty(value = "total_value")
    private BigDecimal totalValue;
    private BigDecimal trend;
    @JsonProperty(value = "total_change")
    private BigDecimal totalChange;
}
