package com.github.ignasbudreika.portfollow.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateStockDTO {
    private String ticker;
    private BigDecimal quantity;
}
