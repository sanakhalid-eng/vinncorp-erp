package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByIdAndWorkspaceId(Long id, Long workspaceId);

    Optional<Employee> findByUserIdAndWorkspaceId(Long userId, Long workspaceId);

    Optional<Employee> findByEmployeeCodeAndWorkspaceId(String employeeCode, Long workspaceId);

    List<Employee> findAllByWorkspaceId(Long workspaceId);

    List<Employee> findAllByWorkspaceIdAndDepartmentId(Long workspaceId, Long departmentId);

    List<Employee> findAllByWorkspaceIdAndStatus(Long workspaceId, com.vinncorp.erp.modules.hr.enums.EmployeeStatus status);

    boolean existsByEmployeeCodeAndWorkspaceId(String employeeCode, Long workspaceId);

    boolean existsByUserIdAndWorkspaceId(Long userId, Long workspaceId);
}


