package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByIdAndWorkspaceId(Long id, Long workspaceId);

    List<Department> findAllByWorkspaceId(Long workspaceId);

    List<Department> findAllByWorkspaceIdAndActiveTrue(Long workspaceId);

    Optional<Department> findByNameAndWorkspaceId(String name, Long workspaceId);

    boolean existsByNameAndWorkspaceId(String name, Long workspaceId);
}


