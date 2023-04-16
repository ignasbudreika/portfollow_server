package com.github.ignasbudreika.portfollow.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorCommentDTO {
    private String id;
    private String author;
    private String comment;
    private boolean deletable;
}
