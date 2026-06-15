package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProject_Id(Long projectId);

    List<ProjectMember> findByProject_IdIn(List<Long> projectIds);

    List<ProjectMember> findByUser_Id(Long userId);

    Optional<ProjectMember> findByProject_IdAndUser_Id(Long projectId, Long userId);

    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);

    long countByProject_IdAndProjectRole_Name(Long projectId, String roleName);

    @Query("""
        select count(p) > 0
        from ProjectMember pm
        join pm.projectRole pr
        join pr.rolePermissions rp
        join rp.permission p
        where pm.project.id = :projectId
        and pm.user.id = :userId
        and p.name = :permission
     """)
    boolean hasPermission(Long projectId, Long userId, String permission);

    @Query("""
        select p.name
        from ProjectMember pm
        join pm.projectRole pr
        join pr.rolePermissions rp
        join rp.permission p
        where pm.project.id = :projectId
        and pm.user.id = :userId
     """)
    List<String> getUserPermissions(Long projectId, Long userId);

    @Query("""
        select pm
        from ProjectMember pm
        where pm.projectRole is null
        and pm.role is not null
     """)
    List<ProjectMember> findMembersWithoutProjectRole();

    Optional<ProjectMember> findByProject_IdAndUser_Email(Long projectId, String email);
}



