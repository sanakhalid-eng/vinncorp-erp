package com.vinncorp.erp.platform.workspace.repository;

import com.vinncorp.erp.platform.workspace.entity.WorkspaceInvitation;
import com.vinncorp.erp.platform.workspace.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, Long> {
    List<WorkspaceInvitation> findByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);
    Optional<WorkspaceInvitation> findByToken(String token);
    List<WorkspaceInvitation> findByStatus(InvitationStatus status);
    List<WorkspaceInvitation> findByWorkspaceIdAndEmailOrderByCreatedAtDesc(Long workspaceId, String email);

    @Query("SELECT i FROM WorkspaceInvitation i WHERE i.status = 'PENDING' AND i.expiresAt < CURRENT_TIMESTAMP")
    List<WorkspaceInvitation> findExpiredPendingInvitations();
}

