package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioInfoDTO {
    private String title;
    private String description;
    @JsonProperty(value = "public")
    private boolean isPublic;
    @JsonProperty(value = "reveal_value")
    private boolean revealValue;
    @JsonProperty(value = "currency_eur")
    private boolean currencyEur;
    @JsonProperty("allowed_users")
    private String[] allowedUsers;
}
