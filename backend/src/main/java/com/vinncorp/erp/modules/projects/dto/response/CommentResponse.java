package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.platform.user.entity.UserSummary;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CommentResponse {

    private Long id;
    private UserSummary author;
    private String content;
    private boolean isEdited;
    private boolean isDeleted;
    private Long parentCommentId;
    private List<CommentResponse> replies;
    private List<Map<String, Object>> reactions;
    private List<UserSummary> mentions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



