package com.vinncorp.erp.modules.crm.service.impl;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.audit.service.AuditService;
import com.vinncorp.erp.modules.crm.entity.Pipeline;
import com.vinncorp.erp.modules.crm.entity.PipelineStage;
import com.vinncorp.erp.modules.crm.repository.PipelineRepository;
import com.vinncorp.erp.modules.crm.repository.PipelineStageRepository;
import com.vinncorp.erp.modules.crm.service.PipelineService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PipelineServiceImpl implements PipelineService {

    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public Pipeline create(Pipeline pipeline, List<PipelineStage> stages, Long workspaceId, String actorEmail) {
        if (pipeline.getName() == null || pipeline.getName().isBlank()) {
            throw new BadRequestException("Pipeline name is required");
        }
        if (pipelineRepository.existsByNameAndWorkspaceId(pipeline.getName(), workspaceId)) {
            throw new ConflictException("Pipeline name already exists");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        pipeline.setWorkspace(workspace);
        pipeline.setCreatedBy(actor.getId());
        pipeline.setUpdatedBy(actor.getId());
        Pipeline saved = pipelineRepository.save(pipeline);

        if (stages != null) {
            int order = 0;
            for (PipelineStage stage : stages) {
                stage.setPipeline(saved);
                stage.setWorkspaceId(workspaceId);
                stage.setDisplayOrder(order++);
                stage.setCreatedBy(actor.getId());
                pipelineStageRepository.save(stage);
            }
        }

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "PIPELINE_CREATED", "PIPELINE", saved.getId(), saved.getName(),
                null, Map.of("name", saved.getName()), null, null);
        return saved;
    }

    @Override
    @Transactional
    public Pipeline update(Long id, Pipeline updated, Long workspaceId, String actorEmail) {
        Pipeline existing = pipelineRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (updated.getName() != null) existing.setName(updated.getName());
        existing.setUpdatedBy(actor.getId());
        return pipelineRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Pipeline get(Long id, Long workspaceId) {
        return pipelineRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pipeline> list(Long workspaceId) {
        return pipelineRepository.findAllByWorkspaceIdOrderByDisplayOrderAsc(workspaceId);
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId, String actorEmail) {
        Pipeline pipeline = pipelineRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline not found: " + id));
        if (pipeline.isDefault()) {
            throw new BadRequestException("Cannot delete the default pipeline");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        pipeline.softDelete(actor.getId());
        pipeline.setUpdatedBy(actor.getId());
        pipelineRepository.save(pipeline);
    }

    @Override
    @Transactional(readOnly = true)
    public Pipeline getDefault(Long workspaceId) {
        return pipelineRepository.findByWorkspaceIdAndIsDefaultTrue(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("No default pipeline found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PipelineStage> getStages(Long pipelineId) {
        return pipelineStageRepository.findByPipelineIdOrderByDisplayOrderAsc(pipelineId);
    }
}
