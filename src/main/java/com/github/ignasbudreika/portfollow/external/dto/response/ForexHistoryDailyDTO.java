package com.github.ignasbudreika.portfollow.external.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForexHistoryDailyDTO {
    @JsonProperty("Time Series FX (Daily)")
    private Map<String, HistoryDTO> history;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryDTO {
        @JsonProperty("4. close")
        private String price;
    }
}
