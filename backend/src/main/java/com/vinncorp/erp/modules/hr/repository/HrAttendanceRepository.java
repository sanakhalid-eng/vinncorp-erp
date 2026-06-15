package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.HrAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HrAttendanceRepository extends JpaRepository<HrAttendance, Long> {

    Optional<HrAttendance> findByIdAndWorkspaceId(Long id, Long workspaceId);

    List<HrAttendance> findAllByWorkspaceIdOrderByAttendanceDateDesc(Long workspaceId);

    List<HrAttendance> findByEmployeeIdAndWorkspaceIdOrderByAttendanceDateDesc(Long employeeId, Long workspaceId);

    List<HrAttendance> findByEmployeeIdAndAttendanceDateBetweenAndWorkspaceId(
            Long employeeId, LocalDate startDate, LocalDate endDate, Long workspaceId);

    List<HrAttendance> findByAttendanceDateAndWorkspaceId(LocalDate date, Long workspaceId);

    Optional<HrAttendance> findByEmployeeIdAndAttendanceDateAndWorkspaceId(
            Long employeeId, LocalDate attendanceDate, Long workspaceId);

    boolean existsByEmployeeIdAndAttendanceDateAndWorkspaceId(
            Long employeeId, LocalDate attendanceDate, Long workspaceId);

    @Query("SELECT COUNT(a) FROM HrAttendance a WHERE a.workspace.id = :workspaceId AND a.attendanceDate = :date AND a.status = :status")
    long countByWorkspaceIdAndDateAndStatus(@Param("workspaceId") Long workspaceId,
                                             @Param("date") LocalDate date,
                                             @Param("status") String status);

    @Query("SELECT COUNT(a) FROM HrAttendance a WHERE a.workspace.id = :workspaceId AND a.attendanceDate = :date")
    long countByWorkspaceIdAndDate(@Param("workspaceId") Long workspaceId,
                                    @Param("date") LocalDate date);

    @Query("SELECT a.status, COUNT(a) FROM HrAttendance a WHERE a.workspace.id = :workspaceId AND a.attendanceDate = :date GROUP BY a.status")
    List<Object[]> countByStatusGrouped(@Param("workspaceId") Long workspaceId,
                                         @Param("date") LocalDate date);
}
