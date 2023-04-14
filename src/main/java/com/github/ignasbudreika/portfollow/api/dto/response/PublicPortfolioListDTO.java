package com.github.ignasbudreika.portfollow.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicPortfolioListDTO {
    private boolean more;
    private int index;
    private PublicPortfolioDTO[] portfolios;
}
