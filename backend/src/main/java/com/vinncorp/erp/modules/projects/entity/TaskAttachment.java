package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.user.entity.User;

import com.vinncorp.erp.core.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "task_attachments", indexes = {
        @Index(name = "idx_attachment_task_id", columnList = "task_id"),
        @Index(name = "idx_attachment_deleted_at", columnList = "deleted_at")
})
@Data
public class TaskAttachment extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "public_id", nullable = false, length = 500)
    private String publicId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    public void softDelete() {
        setDeletedAt(LocalDateTime.now());
    }
}



