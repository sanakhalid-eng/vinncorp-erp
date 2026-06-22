package com.vinncorp.erp.platform.workspace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_usage")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WorkspaceUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false, unique = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private Workspace workspace;

    @Column(nullable = false)
    private int projectCount = 0;

    @Column(nullable = false)
    private int memberCount = 0;

    @Column(nullable = false)
    private long storageUsedBytes = 0;

    @Column(nullable = false)
    private int webhookCount = 0;

    @Column(nullable = false)
    private int apiRequestsThisMonth = 0;

    @Column(nullable = false)
    private LocalDateTime lastCalculatedAt;

    @PrePersist
    protected void onCreate() {
        lastCalculatedAt = LocalDateTime.now();
    }
}

