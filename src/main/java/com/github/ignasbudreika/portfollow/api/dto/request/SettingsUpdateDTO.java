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
public class SettingsUpdateDTO {
    @NotEmpty(message = "username is required")
    @Length(max = 30, message = "username cannot exceed 30 characters")
    private String username;
    @NotEmpty(message = "title is required")
    @Length(max = 30, message = "title cannot exceed 30 characters")
    private String title;
    @Length(max = 100, message = "description cannot exceed 100 characters")
    private String description;
    @JsonProperty("public")
    private boolean isPublic;
    @JsonProperty("hide_value")
    private boolean hideValue;
    @Length(max = 100, message = "allowed users cannot exceed 100 characters")
    @JsonProperty("allowed_users")
    private String allowedUsers;
}
