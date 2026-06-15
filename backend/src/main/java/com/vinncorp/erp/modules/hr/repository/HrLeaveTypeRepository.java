package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.HrLeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HrLeaveTypeRepository extends JpaRepository<HrLeaveType, Long> {

    Optional<HrLeaveType> findByIdAndWorkspaceId(Long id, Long workspaceId);

    List<HrLeaveType> findAllByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);

    List<HrLeaveType> findByWorkspaceIdAndIsActiveTrueOrderByCreatedAtDesc(Long workspaceId);

    boolean existsByNameAndWorkspaceId(String name, Long workspaceId);

    boolean existsByCodeAndWorkspaceId(String code, Long workspaceId);
}
