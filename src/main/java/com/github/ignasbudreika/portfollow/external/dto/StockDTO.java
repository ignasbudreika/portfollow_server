package com.github.ignasbudreika.portfollow.external.dto;

import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@Getter
@NoArgsConstructor
@JsonRootName(value = "Global Quote")
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockDTO {
    @JsonProperty("01. symbol")
    private String symbol;

    @JsonProperty("05. price")
    private String price;

    @JsonProperty("08. previous close")
    private String previousClose;
}
