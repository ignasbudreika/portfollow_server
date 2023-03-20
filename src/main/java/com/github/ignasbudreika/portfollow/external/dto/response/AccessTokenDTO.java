package com.github.ignasbudreika.portfollow.external.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenDTO {
    @JsonProperty("access_token")
    private String accessToken;
}
