package com.github.ignasbudreika.portfollow.api.dto.request;

import com.github.ignasbudreika.portfollow.enums.PeriodicInvestmentPeriod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDTO {
    @NotEmpty(message = "symbol is required")
    private String symbol;
    @Min(value = 0, message = "quantity cannot be less than 0")
    private BigDecimal quantity;
    @Min(value = 0, message = "amount cannot be less than 0")
    private BigDecimal amount;
    private PeriodicInvestmentPeriod period;
    @NotNull(message = "date is required")
    private LocalDate date;
    private boolean crypto;
}
