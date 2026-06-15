package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.AttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    AttachmentResponse uploadAttachment(Long taskId, MultipartFile file, String email);

    List<AttachmentResponse> getAttachmentsByTaskId(Long taskId);

    void deleteAttachment(Long attachmentId, String email);
}



