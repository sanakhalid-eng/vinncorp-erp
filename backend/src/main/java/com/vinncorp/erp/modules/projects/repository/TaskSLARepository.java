package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.TaskSLA;
import com.vinncorp.erp.modules.projects.enums.SLAStatus;
import com.vinncorp.erp.modules.projects.enums.SLAType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskSLARepository extends JpaRepository<TaskSLA, Long> {

    Optional<TaskSLA> findByTaskIdAndSlaType(Long taskId, SLAType slaType);

    List<TaskSLA> findByProjectId(Long projectId);

    List<TaskSLA> findByWorkspaceIdAndStatus(Long workspaceId, SLAStatus status);

    @Query("SELECT s FROM TaskSLA s WHERE s.status = 'ACTIVE' AND s.warningThresholdPct > 0 AND s.createdAt IS NOT NULL")
    List<TaskSLA> findAllActiveWithWarning();

    @Query("SELECT s FROM TaskSLA s WHERE s.status = 'ACTIVE' AND (s.breachedAt IS NOT NULL OR s.warnedAt IS NOT NULL)")
    List<TaskSLA> findAllTriggered();

    long countByWorkspaceIdAndStatus(Long workspaceId, SLAStatus status);

    long countByProjectIdAndStatus(Long projectId, SLAStatus status);
}



