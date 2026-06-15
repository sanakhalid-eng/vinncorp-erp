package com.vinncorp.erp.modules.projects.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vinncorp.erp.core.user.constants.PermissionConstants;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.user.entity.UserSummary;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.workspace.repository.WorkspaceUsageRepository;
import com.vinncorp.erp.modules.projects.dto.response.AttachmentResponse;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskAttachment;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.event.EventPublisher;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.repository.TaskAttachmentRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.modules.projects.service.AttachmentService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ForbiddenOperationException;
import com.vinncorp.erp.shared.exception.FileUploadException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "text/csv",
            "application/zip",
            "application/x-zip-compressed",
            "application/vnd.rar",
            "application/x-tar",
            "application/rtf"
    );

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final Cloudinary cloudinary;
    private final ActivityLogService activityLogService;
    private final EventPublisher eventPublisher;
    private final WorkspaceUsageRepository workspaceUsageRepository;

    @Override
    @Transactional
    public AttachmentResponse uploadAttachment(Long taskId, MultipartFile file, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long projectId = task.getProject().getId();

        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, user.getId())) {
            throw new ForbiddenOperationException("You must be a project member to upload files");
        }

        if (!projectMemberRepository.hasPermission(projectId, user.getId(), PermissionConstants.CREATE_TASK)) {
            throw new ForbiddenOperationException("You do not have permission to upload files to this project");
        }

        validateFile(file);

        try {
            String safeFileName = sanitizeFileName(file.getOriginalFilename());
            String publicId = "task_attachments/" + taskId + "/" + UUID.randomUUID().toString().substring(0, 8);

            var uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "task_attachments",
                            "public_id", publicId,
                            "resource_type", "auto"
                    )
            );

            String fileUrl = (String) uploadResult.get("secure_url");
            String fileType = file.getContentType();
            Long fileSize = file.getSize();

            TaskAttachment attachment = new TaskAttachment();
            attachment.setTask(task);
            attachment.setUploadedBy(user);
            attachment.setFileUrl(fileUrl);
            attachment.setPublicId(publicId);
            attachment.setFileName(safeFileName);
            attachment.setFileType(fileType);
            attachment.setFileSize(fileSize);

            TaskAttachment saved = attachmentRepository.save(attachment);

            workspaceUsageRepository.findByWorkspaceId(task.getProject().getWorkspace().getId())
                    .ifPresent(usage -> {
                        usage.setStorageUsedBytes(usage.getStorageUsedBytes() + fileSize);
                        workspaceUsageRepository.save(usage);
                    });

            assert fileType != null;
            activityLogService.logActivity(
                    user.getId(),
                    EntityType.ATTACHMENT,
                    saved.getId(),
                    ActionType.FILE_UPLOADED,
                    null,
                    Map.of(
                            "fileName", safeFileName,
                            "fileType", fileType,
                            "fileSize", fileSize,
                            "taskId", taskId,
                            "taskTitle", task.getTitle()
                    ),
                    "File uploaded: " + safeFileName,
                    projectId
            );

            if (task.getAssignee() != null && !task.getAssignee().getId().equals(user.getId())) {
                eventPublisher.publish(DomainEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .type(DomainEvent.Type.FILE_UPLOADED)
                        .actorId(user.getId())
                        .targetUserId(task.getAssignee().getId())
                        .entityType("ATTACHMENT")
                        .entityId(saved.getId())
                        .projectId(projectId)
                        .projectName(task.getProject().getName())
                        .message(user.getName() + " uploaded a file to task: " + truncate(task.getTitle(), 40))
                        .metadata(Map.of("fileName", safeFileName, "fileSize", fileSize))
                        .build());
            }

            return toResponse(saved);

        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file to cloud storage");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByTaskId(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }

        return attachmentRepository.findByTaskIdAndDeletedAtIsNullOrderByCreatedAtDesc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, String email) {
        TaskAttachment attachment = attachmentRepository.findByIdAndDeletedAtIsNull(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isUploader = attachment.getUploadedBy().getId().equals(user.getId());
        boolean isProjectManager = attachment.getTask().getProject().getProjectManager() != null
                && attachment.getTask().getProject().getProjectManager().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().contains("ADMIN");

        if (!isUploader && !isProjectManager && !isAdmin) {
            throw new ForbiddenOperationException("Only the uploader or project manager can delete attachments");
        }

        try {
            cloudinary.uploader().destroy(attachment.getPublicId(), ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new FileUploadException("Failed to delete file from cloud storage");
        }

        String fileName = attachment.getFileName();
        Long projectId = attachment.getTask().getProject().getId();
        Long fileSize = attachment.getFileSize();

        attachment.softDelete();
        attachmentRepository.save(attachment);

        workspaceUsageRepository.findByWorkspaceId(attachment.getTask().getProject().getWorkspace().getId())
                .ifPresent(usage -> {
                    usage.setStorageUsedBytes(Math.max(0, usage.getStorageUsedBytes() - fileSize));
                    workspaceUsageRepository.save(usage);
                });

        activityLogService.logActivity(
                user.getId(),
                EntityType.ATTACHMENT,
                attachment.getId(),
                ActionType.DELETED,
                Map.of("fileName", fileName),
                null,
                "File deleted: " + fileName,
                projectId
        );
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size must be less than 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("File type not allowed. Supported: images, PDFs, documents, spreadsheets, text, archives");
        }
    }

    private String sanitizeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "unnamed_file";
        }
        return originalName.replaceAll("[^a-zA-Z0-9._\\- ]", "_").trim();
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private AttachmentResponse toResponse(TaskAttachment attachment) {
        AttachmentResponse response = new AttachmentResponse();
        response.setId(attachment.getId());
        response.setFileUrl(attachment.getFileUrl());
        response.setFileName(attachment.getFileName());
        response.setFileType(attachment.getFileType());
        response.setFileSize(attachment.getFileSize());
        response.setCreatedAt(attachment.getCreatedAt());

        if (attachment.getUploadedBy() != null) {
            UserSummary uploader = new UserSummary();
            uploader.setId(attachment.getUploadedBy().getId());
            uploader.setName(attachment.getUploadedBy().getName());
            uploader.setEmail(attachment.getUploadedBy().getEmail());
            uploader.setAvatarUrl(attachment.getUploadedBy().getAvatarUrl());
            response.setUploadedBy(uploader);
        }

        return response;
    }
}



