package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.HrProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HrProjectAssignmentRepository extends JpaRepository<HrProjectAssignment, Long> {

    Optional<HrProjectAssignment> findByIdAndWorkspaceId(Long id, Long workspaceId);

    List<HrProjectAssignment> findByEmployeeIdAndWorkspaceIdOrderByStartDateDesc(Long employeeId, Long workspaceId);

    List<HrProjectAssignment> findByProjectIdAndWorkspaceIdOrderByStartDateDesc(Long projectId, Long workspaceId);

    List<HrProjectAssignment> findByWorkspaceIdOrderByStartDateDesc(Long workspaceId);

    Optional<HrProjectAssignment> findByEmployeeIdAndProjectIdAndWorkspaceId(Long employeeId, Long projectId, Long workspaceId);

    boolean existsByEmployeeIdAndProjectIdAndWorkspaceId(Long employeeId, Long projectId, Long workspaceId);

    List<HrProjectAssignment> findByEmployeeIdAndStatusAndWorkspaceId(Long employeeId, String status, Long workspaceId);
}
