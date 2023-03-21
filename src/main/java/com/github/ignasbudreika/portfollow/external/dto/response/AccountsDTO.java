package com.github.ignasbudreika.portfollow.external.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AccountsDTO {
    @JsonProperty("accounts")
    private AccountDTO[] accounts;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountDTO {
        @JsonProperty("currencyCode")
        private String currencyCode;
        @JsonProperty("balance")
        private BigDecimal balance;
    }
}
