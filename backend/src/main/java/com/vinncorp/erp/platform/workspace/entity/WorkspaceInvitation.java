package com.vinncorp.erp.platform.workspace.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.workspace.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workspace_invitations", indexes = {
    @Index(name = "idx_workspace_invitation_token", columnList = "token"),
    @Index(name = "idx_workspace_invitation_email", columnList = "email"),
    @Index(name = "idx_workspace_invitation_workspace", columnList = "workspace_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class WorkspaceInvitation extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Workspace workspace;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    @Column(name = "workspace_role")
    private String workspaceRole;

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
        if (token == null) token = UUID.randomUUID().toString();
        if (expiresAt == null) expiresAt = LocalDateTime.now().plusDays(7);
        if (status == null) status = InvitationStatus.PENDING;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}

