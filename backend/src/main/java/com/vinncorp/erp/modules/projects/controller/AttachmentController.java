package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.AttachmentResponse;
import com.vinncorp.erp.modules.projects.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Tasks")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/tasks/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment", description = "Upload a file attachment to a task")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AttachmentResponse response = attachmentService.uploadAttachment(taskId, file, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Attachment uploaded successfully", response));
    }

    @GetMapping("/tasks/{taskId}/attachments")
    @Operation(summary = "Get task attachments", description = "Retrieve all attachments for a task")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachments(
            @PathVariable Long taskId
    ) {
        List<AttachmentResponse> responses = attachmentService.getAttachmentsByTaskId(taskId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attachments fetched successfully", responses));
    }

    @DeleteMapping("/attachments/{id}")
    @Operation(summary = "Delete attachment", description = "Delete an attachment by ID")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        attachmentService.deleteAttachment(id, email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attachment deleted successfully", null));
    }
}



