package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    List<TimeLog> findByTaskIdOrderByLogDateDescCreatedAtDesc(Long taskId);

    List<TimeLog> findByUserIdOrderByLogDateDescCreatedAtDesc(Long userId);

    List<TimeLog> findByUserIdOrderByLogDateDesc(Long userId);

    List<TimeLog> findByUserIdAndWorkspaceIdOrderByLogDateDescCreatedAtDesc(Long userId, Long workspaceId);

    @Query("SELECT tl FROM TimeLog tl WHERE tl.user.id = :userId AND tl.logDate BETWEEN :startDate AND :endDate ORDER BY tl.logDate ASC, tl.createdAt ASC")
    List<TimeLog> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tl FROM TimeLog tl WHERE tl.user.id = :userId AND tl.workspace.id = :workspaceId AND tl.logDate BETWEEN :startDate AND :endDate ORDER BY tl.logDate ASC, tl.createdAt ASC")
    List<TimeLog> findByUserIdAndDateRangeAndWorkspace(@Param("userId") Long userId, @Param("workspaceId") Long workspaceId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.task.id = :taskId")
    BigDecimal getTotalHoursByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.user.id = :userId AND tl.logDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalHoursByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.user.id = :userId AND tl.workspace.id = :workspaceId AND tl.logDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalHoursByUserIdAndDateRangeAndWorkspace(@Param("userId") Long userId, @Param("workspaceId") Long workspaceId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tl.logDate, COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.user.id = :userId AND tl.logDate BETWEEN :startDate AND :endDate GROUP BY tl.logDate ORDER BY tl.logDate ASC")
    List<Object[]> getDailyHoursByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tl.logDate, COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.user.id = :userId AND tl.workspace.id = :workspaceId AND tl.logDate BETWEEN :startDate AND :endDate GROUP BY tl.logDate ORDER BY tl.logDate ASC")
    List<Object[]> getDailyHoursByUserAndDateRangeAndWorkspace(@Param("userId") Long userId, @Param("workspaceId") Long workspaceId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tl.task.id, COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.user.id = :userId AND tl.logDate BETWEEN :startDate AND :endDate GROUP BY tl.task.id")
    List<Object[]> getHoursByTaskForUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tl.task.id, COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.user.id = :userId AND tl.workspace.id = :workspaceId AND tl.logDate BETWEEN :startDate AND :endDate GROUP BY tl.task.id")
    List<Object[]> getHoursByTaskForUserAndDateRangeAndWorkspace(@Param("userId") Long userId, @Param("workspaceId") Long workspaceId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tl.task.id, COALESCE(SUM(tl.hours), 0) FROM TimeLog tl JOIN tl.task t WHERE t.project.id = :projectId GROUP BY tl.task.id")
    List<Object[]> getTaskHoursByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(tl.hours), 0) FROM TimeLog tl JOIN tl.task t WHERE t.project.id = :projectId AND tl.logDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalHoursByProjectIdAndDateRange(@Param("projectId") Long projectId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.user.id = :userId AND tl.logDate = :logDate")
    BigDecimal getTotalHoursByUserIdAndDate(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    @Query("SELECT COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.user.id = :userId AND tl.workspace.id = :workspaceId AND tl.logDate = :logDate")
    BigDecimal getTotalHoursByUserIdAndDateAndWorkspace(@Param("userId") Long userId, @Param("workspaceId") Long workspaceId, @Param("logDate") LocalDate logDate);

    boolean existsByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserIdAndWorkspaceId(Long id, Long userId, Long workspaceId);

    @Query("SELECT tl FROM TimeLog tl JOIN tl.task t WHERE t.project.id = :projectId AND tl.workspace.id = :workspaceId AND tl.logDate BETWEEN :startDate AND :endDate")
    List<TimeLog> findByProjectIdAndWorkspaceIdAndDateRange(@Param("projectId") Long projectId, @Param("workspaceId") Long workspaceId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tl.user.id, COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.workspace.id = :workspaceId AND tl.logDate BETWEEN :startDate AND :endDate GROUP BY tl.user.id")
    List<Object[]> getTotalHoursByWorkspaceAndDateRangeGroupedByUser(@Param("workspaceId") Long workspaceId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tl.task.project.id, COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.workspace.id = :workspaceId AND tl.logDate BETWEEN :startDate AND :endDate AND tl.task.project.id IS NOT NULL GROUP BY tl.task.project.id")
    List<Object[]> getTotalHoursByWorkspaceAndDateRangeGroupedByProject(@Param("workspaceId") Long workspaceId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}



