package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "background_jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class BackgroundJob extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "job_type", nullable = false, length = 100)
    private String jobType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "priority")
    private int priority = 0;

    @Column(name = "max_retries")
    private int maxRetries = 3;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "workspace_id")
    private Long workspaceId;

    @Column(name = "dead_letter", nullable = false)
    private boolean deadLetter = false;

    @Column(name = "dead_letter_reason", columnDefinition = "TEXT")
    private String deadLetterReason;
}



