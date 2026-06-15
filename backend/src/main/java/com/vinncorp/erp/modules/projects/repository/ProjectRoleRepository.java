package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {

    Optional<ProjectRole> findByName(String name);

    @Query("""
        SELECT pr.name
        FROM ProjectRole pr
        JOIN pr.rolePermissions rp
        JOIN rp.permission p
        WHERE pr.id IN :ids
    """)
    List<String> findRoleNamesByIds(@Param("ids") List<Long> ids);
}



