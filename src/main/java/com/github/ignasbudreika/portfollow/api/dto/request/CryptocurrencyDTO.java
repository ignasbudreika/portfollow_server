package com.github.ignasbudreika.portfollow.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CryptocurrencyDTO {
    private String symbol;
    private BigDecimal quantity;
    private LocalDate date;
}
