package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.TaskSprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskSprintRepository extends JpaRepository<TaskSprint, Long> {

    Optional<TaskSprint> findByTaskId(Long taskId);

    List<TaskSprint> findBySprintId(Long sprintId);

    boolean existsByTaskIdAndSprintId(Long taskId, Long sprintId);

    void deleteByTaskId(Long taskId);

    void deleteBySprintId(Long sprintId);

    void deleteByTaskIdAndSprintId(Long taskId, Long sprintId);

    @Query("SELECT ts FROM TaskSprint ts LEFT JOIN FETCH ts.task WHERE ts.sprint.id = :sprintId")
    List<TaskSprint> findBySprintIdWithTasks(@Param("sprintId") Long sprintId);

    @Query("SELECT ts FROM TaskSprint ts LEFT JOIN FETCH ts.sprint WHERE ts.task.id = :taskId")
    Optional<TaskSprint> findByTaskIdWithSprint(@Param("taskId") Long taskId);
}



