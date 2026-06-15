package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "Comment content cannot be empty")
    @Size(max = 5000, message = "Comment content must be less than 5000 characters")
    private String content;

    private Long parentCommentId;
}



