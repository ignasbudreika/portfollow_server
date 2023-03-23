package com.github.ignasbudreika.portfollow.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EthereumWalletConnectionDTO {
    @JsonProperty("address")
    private String address;
}
