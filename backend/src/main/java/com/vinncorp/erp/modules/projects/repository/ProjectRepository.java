package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    long countByWorkspaceIdAndDeletedAtIsNull(Long workspaceId);

    List<Project> findByOwner_Email(String email);

    List<Project> findDistinctByMembers_User_Email(String email);

    // Workspace-scoped queries
    List<Project> findByWorkspaceId(Long workspaceId);

    Page<Project> findByWorkspaceId(Long workspaceId, Pageable pageable);

    List<Project> findByWorkspaceIdAndOwner_Email(Long workspaceId, String email);

    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE p.workspace.id = :workspaceId AND m.user.email = :email")
    List<Project> findDistinctByWorkspaceIdAndMembers_User_Email(@Param("workspaceId") Long workspaceId, @Param("email") String email);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.members pm WHERE p.workspace.id = :workspaceId AND pm.user.email = :email AND p.deletedAt IS NULL")
    Page<Project> findDistinctByWorkspaceIdAndMembers_User_Email(
            @Param("workspaceId") Long workspaceId,
            @Param("email") String email,
            Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.workspace.id = :workspaceId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Project> findRecentByWorkspaceId(@Param("workspaceId") Long workspaceId);

    Optional<Project> findByNameAndWorkspaceIdAndDeletedAtIsNull(String name, Long workspaceId);

    List<Project> findByWorkspaceIdAndCategoryAndDeletedAtIsNull(Long workspaceId, String category);
}



