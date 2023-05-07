package com.github.ignasbudreika.portfollow.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEthereumWalletConnectionDTO {
    @NotEmpty(message = "address is required")
    @Length(max = 42, message = "address cannot exceed 42 characters")
    @JsonProperty("address")
    private String address;
}
