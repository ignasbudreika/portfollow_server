package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioDTO {
    @JsonProperty("total_value")
    private BigDecimal totalValue;
    @JsonProperty("total_change")
    private BigDecimal totalChange;
    private BigDecimal change;
    @JsonProperty("is_empty")
    private boolean isEmpty;
}
