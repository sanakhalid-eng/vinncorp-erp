package com.vinncorp.erp.modules.crm.repository;

import com.vinncorp.erp.modules.crm.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {
    List<PipelineStage> findByPipelineIdOrderByDisplayOrderAsc(Long pipelineId);
    Optional<PipelineStage> findByIdAndWorkspaceId(Long id, Long workspaceId);
    List<PipelineStage> findByWorkspaceIdOrderByDisplayOrderAsc(Long workspaceId);
}
