package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.HrShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HrShiftRepository extends JpaRepository<HrShift, Long> {

    Optional<HrShift> findByIdAndWorkspaceId(Long id, Long workspaceId);

    List<HrShift> findAllByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);

    List<HrShift> findByWorkspaceIdAndIsActiveTrueOrderByCreatedAtDesc(Long workspaceId);

    boolean existsByNameAndWorkspaceId(String name, Long workspaceId);
}
