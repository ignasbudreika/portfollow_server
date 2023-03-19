package com.github.ignasbudreika.portfollow.external.dto;

import lombok.*;
import org.codehaus.jackson.annotate.JsonProperty;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenRequestDTO {
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("version")
    private String version;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
}
