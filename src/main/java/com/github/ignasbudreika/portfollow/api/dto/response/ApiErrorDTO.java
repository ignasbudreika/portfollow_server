package com.github.ignasbudreika.portfollow.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorDTO {
    private String key;
    private String message;
}
