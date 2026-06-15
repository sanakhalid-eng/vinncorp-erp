package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.TaskLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskLabelRepository extends JpaRepository<TaskLabel, Long> {

    @Query("SELECT tl FROM TaskLabel tl JOIN FETCH tl.label l WHERE tl.task.id = :taskId AND l.deletedAt IS NULL ORDER BY l.name ASC")
    List<TaskLabel> findByTaskIdWithActiveLabel(@Param("taskId") Long taskId);

    @Query("SELECT tl FROM TaskLabel tl WHERE tl.task.id = :taskId AND tl.label.deletedAt IS NULL")
    List<TaskLabel> findByTaskIdWithActiveLabelsSimple(@Param("taskId") Long taskId);

    Optional<TaskLabel> findByTaskIdAndLabelId(Long taskId, Long labelId);

    boolean existsByTaskIdAndLabelId(Long taskId, Long labelId);

    List<TaskLabel> findByTaskId(Long taskId);

    List<TaskLabel> findByLabelId(Long labelId);

    @Query("SELECT DISTINCT tl.task.id FROM TaskLabel tl JOIN tl.label l WHERE l.id IN :labelIds AND l.deletedAt IS NULL")
    List<Long> findTaskIdsByActiveLabelIds(@Param("labelIds") List<Long> labelIds);
}



