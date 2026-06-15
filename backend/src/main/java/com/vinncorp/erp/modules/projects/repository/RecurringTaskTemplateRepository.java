package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.RecurringTaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecurringTaskTemplateRepository extends JpaRepository<RecurringTaskTemplate, Long> {

    List<RecurringTaskTemplate> findByProjectIdAndDeletedAtIsNull(Long projectId);

    List<RecurringTaskTemplate> findByWorkspaceIdAndDeletedAtIsNull(Long workspaceId);

    Optional<RecurringTaskTemplate> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT t FROM RecurringTaskTemplate t WHERE t.active = true AND t.paused = false AND t.deletedAt IS NULL AND t.nextRunAt <= :now")
    List<RecurringTaskTemplate> findDueTemplates(@Param("now") LocalDateTime now);

    List<RecurringTaskTemplate> findByTemplateTaskIdAndDeletedAtIsNull(Long templateTaskId);

    boolean existsByTemplateTaskIdAndDeletedAtIsNull(Long templateTaskId);

    @Query("SELECT t FROM RecurringTaskTemplate t WHERE t.projectId = :projectId AND t.deletedAt IS NULL AND t.active = true")
    List<RecurringTaskTemplate> findActiveByProjectId(@Param("projectId") Long projectId);
}



