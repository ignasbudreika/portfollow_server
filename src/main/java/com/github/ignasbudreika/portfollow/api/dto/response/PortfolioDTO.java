package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioDTO {
    private BigDecimal totalValue;
    private List<DistributionDTO> distribution;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DistributionDTO {
        private String label;
        private BigDecimal value;
        private BigDecimal percentage;
    }
}
