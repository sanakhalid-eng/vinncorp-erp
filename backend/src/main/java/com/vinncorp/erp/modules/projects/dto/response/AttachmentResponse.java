package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.platform.user.entity.UserSummary;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentResponse {

    private Long id;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private UserSummary uploadedBy;
    private LocalDateTime createdAt;
}



