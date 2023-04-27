package com.github.ignasbudreika.portfollow.api.dto.request;

import com.github.ignasbudreika.portfollow.enums.PeriodicInvestmentPeriod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockDTO {
    private String ticker;
    private PeriodicInvestmentPeriod period;
    private BigDecimal quantity;
    private LocalDate date;
}
