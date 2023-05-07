package com.github.ignasbudreika.portfollow.api.dto.request;

import com.github.ignasbudreika.portfollow.enums.InvestmentTransactionType;
import jakarta.validation.constraints.Min;
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
public class CreateTransactionDTO {
    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity cannot be less than 0")
    private BigDecimal quantity;
    @NotNull(message = "tx type is required")
    private InvestmentTransactionType type;
    @NotNull(message = "tx date is required")
    private LocalDate date;
}
