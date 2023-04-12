package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ignasbudreika.portfollow.enums.ConnectionStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EthereumWalletConnectionDTO {
    private String address;
    @JsonProperty("updated_at")
    private String lastFetched;
    private ConnectionStatus status;
}
