package com.github.ignasbudreika.portfollow.external.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@Getter
@NoArgsConstructor
@JsonRootName("Realtime Currency Exchange Rate")
public class CryptocurrencyDTO {
    @JsonProperty("1. From_Currency Code")
    private String cryptocurrencyCode;
    @JsonProperty("3. To_Currency Code")
    private String baseCurrencyCode;
    @JsonProperty("5. Exchange Rate")
    private String exchangeRate;
}
