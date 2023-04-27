package com.github.ignasbudreika.portfollow.external.dto.response;

import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsDTO {
    @JsonProperty("accounts")
    private AccountDTO[] accounts = new AccountDTO[]{};

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountDTO {
        @JsonProperty("currencyCode")
        private String currencyCode;
        @JsonProperty("balance")
        private BigDecimal balance;
    }
}
