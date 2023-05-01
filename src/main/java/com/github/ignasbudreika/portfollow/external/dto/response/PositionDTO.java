package com.github.ignasbudreika.portfollow.external.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionDTO {
    @JsonProperty("asset_class")
    private String assetClass;
    private String symbol;
    @JsonProperty("qty")
    private String quantity;
}
