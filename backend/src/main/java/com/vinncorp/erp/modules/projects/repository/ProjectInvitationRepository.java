package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.core.workspace.enums.InvitationStatus;
import com.vinncorp.erp.modules.projects.entity.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {

    Optional<ProjectInvitation> findByToken(String token);

    List<ProjectInvitation> findByStatus(InvitationStatus status);

    List<ProjectInvitation> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<ProjectInvitation> findByProjectIdAndEmailOrderByCreatedAtDesc(Long projectId, String email);

    List<ProjectInvitation> findByEmailAndStatus(String email, InvitationStatus status);

    List<ProjectInvitation> findByEmailOrderByCreatedAtDesc(String email);

    boolean existsByProjectIdAndEmailAndStatus(Long projectId, String email, InvitationStatus status);

    @Query("SELECT i FROM ProjectInvitation i WHERE i.status = 'PENDING' AND i.deletedAt IS NULL AND i.expiresAt < CURRENT_TIMESTAMP")
    List<ProjectInvitation> findExpiredPendingInvitations();
}



