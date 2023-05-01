package com.github.ignasbudreika.portfollow.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlpacaConnectionDTO {
    @JsonProperty("api_key")
    private String apiKey;
    @JsonProperty("secret")
    private String secret;
}
