package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.CommentRequest;
import com.vinncorp.erp.modules.projects.dto.request.ReactionRequest;
import com.vinncorp.erp.modules.projects.dto.response.CommentResponse;
import com.vinncorp.erp.modules.projects.dto.response.EditHistoryResponse;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(Long taskId, CommentRequest request, String email);

    List<CommentResponse> getCommentsByTaskId(Long taskId);

    CommentResponse updateComment(Long commentId, CommentRequest request, String email);

    void deleteComment(Long commentId, String email);

    CommentResponse toggleReaction(Long commentId, ReactionRequest request, String email);

    List<EditHistoryResponse> getEditHistory(Long commentId, String email);

    int getCommentCountForTask(Long taskId);
}



