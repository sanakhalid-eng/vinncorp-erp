package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.workspace.entity.Workspace;

import com.vinncorp.erp.core.audit.BaseAuditableEntity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "time_logs", indexes = {
        @Index(name = "idx_time_log_task_id", columnList = "task_id"),
        @Index(name = "idx_time_log_user_id", columnList = "user_id"),
        @Index(name = "idx_time_log_log_date", columnList = "log_date")
})
@Data
public class TimeLog extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal hours;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Workspace workspace;

    @Column(name = "workspace_id", insertable = false, updatable = false)
    private Long workspaceId;
}



