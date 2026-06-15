package com.vinncorp.erp.modules.crm.service;

import com.vinncorp.erp.modules.crm.entity.Pipeline;
import com.vinncorp.erp.modules.crm.entity.PipelineStage;
import java.util.List;

public interface PipelineService {
    Pipeline create(Pipeline pipeline, List<PipelineStage> stages, Long workspaceId, String actorEmail);
    Pipeline update(Long id, Pipeline pipeline, Long workspaceId, String actorEmail);
    Pipeline get(Long id, Long workspaceId);
    List<Pipeline> list(Long workspaceId);
    void delete(Long id, Long workspaceId, String actorEmail);
    Pipeline getDefault(Long workspaceId);
    List<PipelineStage> getStages(Long pipelineId);
}
