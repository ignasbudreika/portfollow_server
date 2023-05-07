package com.github.ignasbudreika.portfollow.api.dto.request;

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
public class CommentDTO {
    @NotEmpty(message = "comment is required")
    @Length(max = 40, message = "comment cannot exceed 40 characters")
    private String comment;
}
