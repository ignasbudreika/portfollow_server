package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionsDTO {
    @JsonProperty("spectrocoin")
    private SpectroCoinConnectionDTO spectroCoinConnection;
    @JsonProperty("ethereum")
    private EthereumWalletConnectionDTO ethereumWalletConnection;
}
