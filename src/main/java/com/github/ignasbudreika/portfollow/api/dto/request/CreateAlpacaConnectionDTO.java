package com.github.ignasbudreika.portfollow.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlpacaConnectionDTO {
    @NotEmpty(message = "API key is required")
    @JsonProperty("api_key")
    private String apiKey;
    @NotEmpty(message = "API secret is required")
    @JsonProperty("secret")
    private String secret;
}
