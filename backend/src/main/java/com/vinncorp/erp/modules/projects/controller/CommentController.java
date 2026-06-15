package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.CommentRequest;
import com.vinncorp.erp.modules.projects.dto.request.ReactionRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.CommentResponse;
import com.vinncorp.erp.modules.projects.dto.response.EditHistoryResponse;
import com.vinncorp.erp.modules.projects.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Tasks")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Create comment", description = "Create a new comment on a task")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        CommentResponse response = commentService.createComment(taskId, request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Comment created successfully", response));
    }

    @GetMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Get task comments", description = "Retrieve all comments for a task")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long taskId
    ) {
        List<CommentResponse> responses = commentService.getCommentsByTaskId(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comments fetched successfully", responses));
    }

    @GetMapping("/tasks/{taskId}/comments/count")
    @Operation(summary = "Get comment count", description = "Get the number of comments on a task")
    public ResponseEntity<ApiResponse<Integer>> getCommentCount(
            @PathVariable Long taskId
    ) {
        int count = commentService.getCommentCountForTask(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comment count fetched", count));
    }

    @PutMapping("/comments/{id}")
    @Operation(summary = "Update comment", description = "Update an existing comment")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        CommentResponse response = commentService.updateComment(id, request, email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comment updated successfully", response));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "Delete comment", description = "Delete a comment by ID")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        commentService.deleteComment(id, email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comment deleted successfully", null));
    }

    @PostMapping("/comments/{id}/reactions")
    @Operation(summary = "Toggle reaction", description = "Toggle a reaction on a comment")
    public ResponseEntity<ApiResponse<CommentResponse>> toggleReaction(
            @PathVariable Long id,
            @Valid @RequestBody ReactionRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        CommentResponse response = commentService.toggleReaction(id, request, email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reaction toggled", response));
    }

    @GetMapping("/comments/{id}/history")
    @Operation(summary = "Get edit history", description = "Retrieve edit history of a comment")
    public ResponseEntity<ApiResponse<List<EditHistoryResponse>>> getEditHistory(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        List<EditHistoryResponse> history = commentService.getEditHistory(id, email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Edit history fetched", history));
    }
}



