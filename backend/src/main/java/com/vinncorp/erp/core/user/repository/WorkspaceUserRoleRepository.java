package com.vinncorp.erp.core.user.repository;

import com.vinncorp.erp.core.user.entity.WorkspaceUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceUserRoleRepository extends JpaRepository<WorkspaceUserRole, Long> {

    List<WorkspaceUserRole> findByWorkspaceIdAndUserIdAndDeletedAtIsNull(Long workspaceId, Long userId);

    List<WorkspaceUserRole> findByUserIdAndDeletedAtIsNull(Long userId);

    Optional<WorkspaceUserRole> findByWorkspaceIdAndUserIdAndRoleIdAndDeletedAtIsNull(
            Long workspaceId, Long userId, Long roleId);

    boolean existsByWorkspaceIdAndUserIdAndRoleIdAndDeletedAtIsNull(
            Long workspaceId, Long userId, Long roleId);

    void deleteByWorkspaceIdAndUserIdAndRoleIdAndDeletedAtIsNull(
            Long workspaceId, Long userId, Long roleId);

    @Query("SELECT wur.role.name FROM WorkspaceUserRole wur " +
           "WHERE wur.workspace.id = :workspaceId AND wur.user.id = :userId AND wur.deletedAt IS NULL")
    List<String> findRoleNamesByWorkspaceAndUser(@Param("workspaceId") Long workspaceId, @Param("userId") Long userId);

    @Query("SELECT DISTINCT p.name FROM WorkspaceUserRole wur " +
           "JOIN wur.role r " +
           "JOIN r.permissions p " +
           "WHERE wur.workspace.id = :workspaceId AND wur.user.id = :userId AND wur.deletedAt IS NULL")
    List<String> findPermissionNamesByWorkspaceAndUser(@Param("workspaceId") Long workspaceId, @Param("userId") Long userId);
}
