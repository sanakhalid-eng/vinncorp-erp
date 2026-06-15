package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {

    List<WorkflowTransition> findByProjectId(Long projectId);

    boolean existsByProjectIdAndFromStatusIdAndToStatusId(
            Long projectId,
            Long fromStatusId,
            Long toStatusId
    );
}


