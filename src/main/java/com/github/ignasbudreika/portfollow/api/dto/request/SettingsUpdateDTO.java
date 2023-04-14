package com.github.ignasbudreika.portfollow.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettingsUpdateDTO {
    private String username;
    private String title;
    private String description;
    @JsonProperty("public")
    private boolean isPublic;
    @JsonProperty("hide_value")
    private boolean hideValue;
    @JsonProperty("allowed_users")
    private String allowedUsers;
    @JsonProperty("currency_eur")
    private boolean currencyEur;
}
