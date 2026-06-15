package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.enums.DependencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    List<TaskDependency> findByTaskIdAndDeletedAtIsNull(Long taskId);

    List<TaskDependency> findByDependsOnTaskIdAndDeletedAtIsNull(Long dependsOnTaskId);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.dependsOnTask WHERE td.task.id = :taskId AND td.deletedAt IS NULL")
    List<TaskDependency> findByTaskIdWithDependsOn(@Param("taskId") Long taskId);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.task WHERE td.dependsOnTask.id = :dependsOnTaskId AND td.deletedAt IS NULL")
    List<TaskDependency> findByDependsOnTaskIdWithTask(@Param("dependsOnTaskId") Long dependsOnTaskId);

    @Query("SELECT td FROM TaskDependency td WHERE td.task.id = :taskId AND td.dependsOnTask.id = :dependsOnTaskId AND td.deletedAt IS NULL")
    Optional<TaskDependency> findByTaskIdAndDependsOnTaskId(@Param("taskId") Long taskId, @Param("dependsOnTaskId") Long dependsOnTaskId);

    boolean existsByTaskIdAndDependsOnTaskIdAndDeletedAtIsNull(Long taskId, Long dependsOnTaskId);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.dependsOnTask WHERE td.task.id = :taskId AND td.dependencyType = :type AND td.deletedAt IS NULL")
    List<TaskDependency> findByTaskIdAndType(@Param("taskId") Long taskId, @Param("type") DependencyType type);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.task WHERE td.dependsOnTask.id = :taskId AND td.dependencyType = :type AND td.deletedAt IS NULL")
    List<TaskDependency> findByDependsOnTaskIdAndType(@Param("taskId") Long taskId, @Param("type") DependencyType type);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.task t LEFT JOIN FETCH td.dependsOnTask d WHERE (td.task.id = :taskId OR td.dependsOnTask.id = :taskId) AND td.deletedAt IS NULL")
    List<TaskDependency> findAllByTaskIdOrDependsOnTaskId(@Param("taskId") Long taskId);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.task t LEFT JOIN FETCH td.dependsOnTask d " +
           "WHERE (td.task.id IN :taskIds OR td.dependsOnTask.id IN :taskIds) AND td.deletedAt IS NULL")
    List<TaskDependency> findByAnyTaskIdIn(@Param("taskIds") List<Long> taskIds);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.dependsOnTask " +
           "WHERE td.task.id = :taskId AND td.dependencyType IN ('BLOCKED_BY') AND td.deletedAt IS NULL " +
           "AND td.dependsOnTask.id IN (SELECT t.id FROM Task t WHERE t.deletedAt IS NULL)")
    List<TaskDependency> findBlockingDependenciesForTask(@Param("taskId") Long taskId);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.task " +
           "WHERE td.dependsOnTask.id = :taskId AND td.dependencyType IN ('BLOCKS') AND td.deletedAt IS NULL " +
           "AND td.task.id IN (SELECT t.id FROM Task t WHERE t.deletedAt IS NULL)")
    List<TaskDependency> findBlockedByDependenciesForTask(@Param("taskId") Long taskId);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.task t LEFT JOIN FETCH td.dependsOnTask d " +
           "WHERE td.task.id = :taskId AND td.dependencyType = 'RELATES_TO' AND td.deletedAt IS NULL")
    List<TaskDependency> findRelatedByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.task t LEFT JOIN FETCH td.dependsOnTask d " +
           "WHERE td.dependsOnTask.id = :taskId AND td.dependencyType = 'RELATES_TO' AND td.deletedAt IS NULL")
    List<TaskDependency> findRelatedByDependsOnTaskId(@Param("taskId") Long taskId);

    @Query("SELECT td FROM TaskDependency td LEFT JOIN FETCH td.task t LEFT JOIN FETCH td.dependsOnTask d " +
           "WHERE td.task.project.id = :projectId AND td.deletedAt IS NULL")
    List<TaskDependency> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT DISTINCT td.task.id FROM TaskDependency td WHERE td.task.project.id = :projectId AND td.deletedAt IS NULL")
    List<Long> findDistinctTaskIdsByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT td.task.id, COUNT(td) FROM TaskDependency td WHERE td.task.id IN :taskIds AND td.deletedAt IS NULL GROUP BY td.task.id")
    List<Object[]> countByTaskIds(@Param("taskIds") List<Long> taskIds);

    @Query("SELECT td.dependsOnTask.id, COUNT(td) FROM TaskDependency td WHERE td.dependsOnTask.id IN :taskIds AND td.deletedAt IS NULL GROUP BY td.dependsOnTask.id")
    List<Object[]> countBlockedByTaskIds(@Param("taskIds") List<Long> taskIds);

    List<TaskDependency> findByTaskId(Long taskId);

    long countByTaskId(Long taskId);

    List<TaskDependency> findByDependsOnTaskId(Long dependsOnTaskId);

    boolean existsByTaskIdAndDependsOnTaskId(Long taskId, Long dependsOnTaskId);

    void deleteByTaskIdAndDependsOnTaskId(Long taskId, Long dependsOnTaskId);
}



