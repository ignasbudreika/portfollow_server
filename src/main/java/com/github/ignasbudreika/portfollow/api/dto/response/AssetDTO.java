package com.github.ignasbudreika.portfollow.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AssetDTO {
    private String symbol;
    private BigDecimal price;
}
