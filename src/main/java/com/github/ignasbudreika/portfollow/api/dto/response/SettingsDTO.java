package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsDTO {
    @JsonProperty("user_info")
    private UserInfoDTO userInfo;
    @JsonProperty("portfolio_info")
    private PortfolioInfoDTO portfolioInfo;
}
