package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicPortfolioDistributionDTO {
    @JsonProperty(value = "hidden_value")
    private boolean hiddenValue;
    private PortfolioDistributionDTO[] distribution;
}
