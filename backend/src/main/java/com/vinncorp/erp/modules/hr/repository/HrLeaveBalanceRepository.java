package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.HrLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HrLeaveBalanceRepository extends JpaRepository<HrLeaveBalance, Long> {

    Optional<HrLeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(Long employeeId, Long leaveTypeId, Integer year);

    List<HrLeaveBalance> findByEmployeeIdAndYearOrderByCreatedAtDesc(Long employeeId, Integer year);

    List<HrLeaveBalance> findByWorkspaceIdAndYearOrderByEmployeeIdAsc(Long workspaceId, Integer year);

    boolean existsByEmployeeIdAndLeaveTypeIdAndYear(Long employeeId, Long leaveTypeId, Integer year);
}
