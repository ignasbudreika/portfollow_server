package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.api.client.util.DateTime;
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
    private DateTime date;
    private BigDecimal value;
    private BigDecimal percentage;
    private InvestmentType type;
}
