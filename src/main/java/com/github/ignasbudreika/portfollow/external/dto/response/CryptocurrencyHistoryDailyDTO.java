package com.github.ignasbudreika.portfollow.external.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptocurrencyHistoryDailyDTO {
    @JsonProperty("Time Series (Digital Currency Daily)")
    private Map<String, StockHistoryDailyDTO.HistoryDTO> history;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryDTO {
        @JsonProperty("4a. close (EUR)")
        private String price;
    }
}
