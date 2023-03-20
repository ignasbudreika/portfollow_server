package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.ignasbudreika.portfollow.enums.SpectroCoinConnectionStatus;
import lombok.Builder;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpectroCoinConnectionDTO {
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("updated_at")
    private LocalDateTime lastFetched;
    private SpectroCoinConnectionStatus status;
}
