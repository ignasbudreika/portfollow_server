package com.github.ignasbudreika.portfollow.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockDTO {
    private String ticker;
    private BigDecimal quantity;
}
