package com.github.ignasbudreika.portfollow.external.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptocurrencyHistoryDailyDTO {
    @JsonProperty("Time Series (Digital Currency Daily)")
    private Map<String, HistoryDTO> history;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryDTO {
        @JsonProperty("4a. close (USD)")
        private String price;
    }
}
