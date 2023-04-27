package com.github.ignasbudreika.portfollow.api.dto.request;

import com.github.ignasbudreika.portfollow.enums.PeriodicInvestmentPeriod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CurrencyDTO {
    private String symbol;
    private BigDecimal quantity;
    private PeriodicInvestmentPeriod period;
    private LocalDate date;
    private boolean crypto;
}
