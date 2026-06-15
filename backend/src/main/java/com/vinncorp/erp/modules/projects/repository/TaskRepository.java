package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface TaskRepository extends
        JpaRepository<Task, Long>,
        JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.statusEntity LEFT JOIN FETCH t.project WHERE t.project.id = :projectId AND t.deletedAt IS NULL")
    List<Task> findByProjectIdWithAssigneeAndStatus(@Param("projectId") Long projectId);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.statusEntity LEFT JOIN FETCH t.project WHERE t.project.id IN :projectIds AND t.deletedAt IS NULL")
    List<Task> findByProjectIdIn(@Param("projectIds") List<Long> projectIds);

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.statusEntity WHERE t.assignee.id = :assigneeId AND t.deletedAt IS NULL")
    List<Task> findByAssignee_Id(@Param("assigneeId") Long assigneeId);

    @Query("SELECT DISTINCT t FROM Task t WHERE t.assignee.id = :assigneeId AND t.deletedAt IS NULL ORDER BY t.updatedAt DESC, t.createdAt DESC")
    List<Task> findTop5ByAssignee_Id(@Param("assigneeId") Long assigneeId, Sort sort);

    Page<Task> findByAssigneeId(Long userId, Pageable pageable);

    Page<Task> findByProjectIdAndAssigneeId(
            Long projectId,
            Long assigneeId,
            Pageable pageable
    );

    Page<Task> findByProjectIdAndDueDateBetween(
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<Task> findByProjectIdAndAssigneeIdAndDueDateBetween(
            Long projectId,
            Long assigneeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    long countByStatusEntity_Id(Long statusId);

    List<Task> findByColumnIdOrderByPositionAsc(Long columnId);

    List<Task> findByDueDateBetweenAndAssigneeIsNotNull(LocalDateTime start, LocalDateTime end);

    List<Task> findByParentTaskIdOrderByCreatedAtAsc(Long parentTaskId);

    long countByParentTaskId(Long parentTaskId);

    long countByParentTaskIdAndStatusEntity_Id(Long parentTaskId, Long statusId);

    boolean existsByParentTaskId(Long parentTaskId);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.taskLabels tl LEFT JOIN FETCH tl.label l WHERE t.id = :id AND t.deletedAt IS NULL AND l.deletedAt IS NULL")
    Optional<Task> findByIdWithLabels(@Param("id") Long id);

    @Query("SELECT t FROM Task t WHERE t.parentTask.id = :parentTaskId AND t.deletedAt IS NULL ORDER BY t.createdAt ASC")
    List<Task> findSubtasksByParentId(@Param("parentTaskId") Long parentTaskId);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.taskLabels tl LEFT JOIN FETCH tl.label l WHERE t.parentTask.id = :parentTaskId AND t.deletedAt IS NULL AND l.deletedAt IS NULL ORDER BY t.createdAt ASC")
    List<Task> findSubtasksWithLabelsByParentId(@Param("parentTaskId") Long parentTaskId);

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.parentTask.id = :taskId AND t.statusEntity.id != :doneStatusId AND t.deletedAt IS NULL")
    boolean existsIncompleteSubtask(@Param("taskId") Long taskId, @Param("doneStatusId") Long doneStatusId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.deletedAt IS NULL AND t.id NOT IN (SELECT ts.task.id FROM TaskSprint ts) AND t.parentTask IS NULL")
    List<Task> findBacklogTasks(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.deletedAt IS NULL AND t.id NOT IN (SELECT ts.task.id FROM TaskSprint ts) AND t.parentTask IS NULL")
    long countBacklogTasks(@Param("projectId") Long projectId);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :start AND :end AND t.deletedAt IS NULL AND t.reminderSent = false AND t.assignee IS NOT NULL")
    List<Task> findTasksDueSoonAndReminderNotSent(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :assigneeId AND t.project.id = :projectId AND t.deletedAt IS NULL")
    long countByAssigneeIdAndProjectIdAndActive(@Param("assigneeId") Long assigneeId, @Param("projectId") Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :assigneeId AND t.dueDate < :date AND t.deletedAt IS NULL")
    long countByAssigneeIdAndDueDateBeforeAndCompletedFalse(@Param("assigneeId") Long assigneeId, @Param("date") LocalDateTime date);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.dueDate < :dueDate AND t.deletedAt IS NULL")
    List<Task> findByProjectIdAndDueDateBeforeAndCompletedFalse(@Param("projectId") Long projectId, @Param("dueDate") LocalDateTime dueDate);

    List<Task> findByParentTaskId(Long parentTaskId);

    @Query(value = "SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.statusEntity LEFT JOIN FETCH t.project WHERE t.project.id = :projectId AND t.deletedAt IS NULL",
           countQuery = "SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.deletedAt IS NULL")
    Page<Task> findByProjectIdWithFetch(@Param("projectId") Long projectId, Pageable pageable);

    @Query(value = "SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.statusEntity WHERE t.assignee.id = :assigneeId AND t.deletedAt IS NULL",
           countQuery = "SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :assigneeId AND t.deletedAt IS NULL")
    Page<Task> findByAssigneeIdWithFetch(@Param("assigneeId") Long assigneeId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds AND t.deletedAt IS NULL AND t.id NOT IN (SELECT ts.task.id FROM TaskSprint ts)")
    List<Task> findByProjectIdInAndNoSprint(@Param("projectIds") List<Long> projectIds);
}



