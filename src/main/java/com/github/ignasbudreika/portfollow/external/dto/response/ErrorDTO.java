package com.github.ignasbudreika.portfollow.external.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

@Getter
@NoArgsConstructor
public class ErrorDTO {
    @JsonProperty("Error Message")
    String message;
}
