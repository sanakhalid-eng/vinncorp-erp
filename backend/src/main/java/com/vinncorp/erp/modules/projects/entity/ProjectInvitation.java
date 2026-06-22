package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.workspace.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_invitations", indexes = {
    @Index(name = "idx_invitation_token", columnList = "token"),
    @Index(name = "idx_invitation_email", columnList = "email"),
    @Index(name = "idx_invitation_project", columnList = "project_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInvitation extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private ProjectRole role;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @PrePersist

    public void prePersist() {
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(7);
        }
        if (status == null) {
            status = InvitationStatus.PENDING;
        }
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

}



