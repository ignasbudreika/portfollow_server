package com.github.ignasbudreika.portfollow.external.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("Realtime Currency Exchange Rate")
public class ForexDTO {
    @JsonProperty("1. From_Currency Code")
    private String currencyCode;
    @JsonProperty("3. To_Currency Code")
    private String baseCurrencyCode;
    @JsonProperty("5. Exchange Rate")
    private String exchangeRate;
}
