package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvestmentDTO {
    private String id;
    private String symbol;
    private BigDecimal value;
    private InvestmentType type;
}
