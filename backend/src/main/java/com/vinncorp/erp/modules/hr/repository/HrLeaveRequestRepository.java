package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.HrLeaveRequest;
import com.vinncorp.erp.modules.hr.enums.LeaveRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HrLeaveRequestRepository extends JpaRepository<HrLeaveRequest, Long> {

    List<HrLeaveRequest> findByWorkspaceIdAndStatusOrderByStartDateDesc(Long workspaceId, LeaveRequestStatus status);

    List<HrLeaveRequest> findByWorkspaceIdOrderByStartDateDesc(Long workspaceId);

    List<HrLeaveRequest> findByEmployeeIdAndStatusOrderByStartDateDesc(Long employeeId, LeaveRequestStatus status);

    List<HrLeaveRequest> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    List<HrLeaveRequest> findByLeaveTypeIdAndStartDateBetweenAndStatusIn(Long leaveTypeId, LocalDate startDate, LocalDate endDate, List<LeaveRequestStatus> statuses);

    @Query("SELECT lr FROM HrLeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status IN :statuses AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<HrLeaveRequest> findOverlappingRequests(@Param("employeeId") Long employeeId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("statuses") List<LeaveRequestStatus> statuses);

    @Query("SELECT COUNT(lr) FROM HrLeaveRequest lr WHERE lr.workspace.id = :workspaceId AND lr.status = :status")
    long countByWorkspaceIdAndStatus(@Param("workspaceId") Long workspaceId, @Param("status") LeaveRequestStatus status);

    @Query("SELECT COALESCE(SUM(lr.totalDays), 0) FROM HrLeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.leaveType.id = :leaveTypeId AND lr.year = :year AND lr.status = :status")
    java.math.BigDecimal sumDaysByEmployeeAndTypeAndYearAndStatus(
            @Param("employeeId") Long employeeId,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("year") Integer year,
            @Param("status") LeaveRequestStatus status);
}
