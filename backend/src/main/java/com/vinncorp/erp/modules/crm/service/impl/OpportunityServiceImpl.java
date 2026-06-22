package com.vinncorp.erp.modules.crm.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.audit.service.AuditService;
import com.vinncorp.erp.modules.crm.entity.Customer;
import com.vinncorp.erp.modules.crm.entity.Opportunity;
import com.vinncorp.erp.modules.crm.entity.PipelineStage;
import com.vinncorp.erp.modules.crm.event.DealLostEvent;
import com.vinncorp.erp.modules.crm.event.DealWonEvent;
import com.vinncorp.erp.modules.crm.repository.CustomerRepository;
import com.vinncorp.erp.modules.crm.repository.OpportunityRepository;
import com.vinncorp.erp.modules.crm.repository.PipelineStageRepository;
import com.vinncorp.erp.modules.crm.service.CrmProjectIntegrationService;
import com.vinncorp.erp.modules.crm.service.OpportunityService;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityServiceImpl implements OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AuditService auditService;
    private final CrmProjectIntegrationService projectIntegrationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Opportunity create(Opportunity opp, Long workspaceId, String actorEmail) {
        if (opp.getTitle() == null || opp.getTitle().isBlank()) {
            throw new BadRequestException("Opportunity title is required");
        }
        if (opp.getStage() == null) {
            throw new BadRequestException("Pipeline stage is required");
        }
        PipelineStage stage = pipelineStageRepository.findByIdAndWorkspaceId(opp.getStage().getId(), workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline stage not found"));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        opp.setWorkspace(workspace);
        opp.setStage(stage);
        opp.setOwnerId(actor.getId());
        opp.setCreatedBy(actor.getId());
        opp.setUpdatedBy(actor.getId());
        Opportunity saved = opportunityRepository.save(opp);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "CREATED", "OPPORTUNITY", saved.getId(), saved.getTitle(),
                null, Map.of("value", saved.getValue(), "stage", stage.getName()),
                null, null);
        return saved;
    }

    @Override
    @Transactional
    public Opportunity update(Long id, Opportunity updated, Long workspaceId, String actorEmail) {
        Opportunity existing = opportunityRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getValue() != null) existing.setValue(updated.getValue());
        if (updated.getCurrency() != null) existing.setCurrency(updated.getCurrency());
        if (updated.getCustomerId() != null) existing.setCustomerId(updated.getCustomerId());
        if (updated.getExpectedCloseDate() != null) existing.setExpectedCloseDate(updated.getExpectedCloseDate());
        if (updated.getProbabilityPct() > 0) existing.setProbabilityPct(updated.getProbabilityPct());
        if (updated.getNotes() != null) existing.setNotes(updated.getNotes());
        existing.setUpdatedBy(actor.getId());
        Opportunity saved = opportunityRepository.save(existing);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "UPDATED", "OPPORTUNITY", saved.getId(), saved.getTitle(), null, null, null, null);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Opportunity get(Long id, Long workspaceId) {
        return opportunityRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Opportunity> list(Long workspaceId) {
        return opportunityRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId);
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId, String actorEmail) {
        Opportunity opp = opportunityRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        opp.softDelete(actor.getId());
        opp.setUpdatedBy(actor.getId());
        opportunityRepository.save(opp);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "DELETED", "OPPORTUNITY", opp.getId(), opp.getTitle(), null, null, null, null);
    }

    @Override
    @Transactional
    public Opportunity changeStage(Long id, Long stageId, Long workspaceId, String actorEmail) {
        Opportunity opp = opportunityRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + id));
        PipelineStage newStage = pipelineStageRepository.findByIdAndWorkspaceId(stageId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline stage not found"));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String oldStage = opp.getStage() != null ? opp.getStage().getName() : "unknown";
        opp.setStage(newStage);
        opp.setProbabilityPct(newStage.getProbabilityPct());
        opp.setUpdatedBy(actor.getId());
        Opportunity saved = opportunityRepository.save(opp);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "PIPELINE_STAGE_CHANGED", "OPPORTUNITY", saved.getId(), saved.getTitle(),
                Map.of("stage", oldStage), Map.of("stage", newStage.getName()),
                null, null);
        return saved;
    }

    @Override
    @Transactional
    public Opportunity won(Long id, Long workspaceId, String actorEmail) {
        Opportunity opp = opportunityRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + id));
        if (opp.getStage() != null && opp.getStage().isWon()) {
            throw new BadRequestException("Opportunity is already won");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Find the Won stage from the same pipeline
        PipelineStage wonStage = pipelineStageRepository.findByPipelineIdOrderByDisplayOrderAsc(opp.getStage().getPipeline().getId())
                .stream()
                .filter(PipelineStage::isWon)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No 'Won' stage found in this pipeline"));

        opp.setStage(wonStage);
        opp.setActualCloseDate(LocalDate.now());
        opp.setProbabilityPct(100);
        opp.setUpdatedBy(actor.getId());
        Opportunity saved = opportunityRepository.save(opp);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "DEAL_WON", "OPPORTUNITY", saved.getId(), saved.getTitle(),
                null, Map.of("value", saved.getValue(), "closeDate", saved.getActualCloseDate().toString()),
                null, null);

        // CRM -> Project Integration: auto-create project
        Customer customer = saved.getCustomerId() != null
                ? customerRepository.findById(saved.getCustomerId()).orElse(null)
                : null;
        Project project = null;
        try {
            project = projectIntegrationService.createProjectFromOpportunity(saved, customer, actorEmail);
        } catch (Exception e) {
            log.error("Project creation failed for opportunity {}: {}", id, e.getMessage());
        }

        // Publish event for notifications
        eventPublisher.publishEvent(new DealWonEvent(
                this, saved.getId(), workspaceId, actor.getId(), actorEmail,
                saved.getTitle(), saved.getValue(),
                project != null ? project.getId() : null
        ));

        return saved;
    }

    @Override
    @Transactional
    public Opportunity lost(Long id, Long workspaceId, String actorEmail) {
        Opportunity opp = opportunityRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + id));
        if (opp.getStage() != null && opp.getStage().isLost()) {
            throw new BadRequestException("Opportunity is already lost");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PipelineStage lostStage = pipelineStageRepository.findByPipelineIdOrderByDisplayOrderAsc(opp.getStage().getPipeline().getId())
                .stream()
                .filter(PipelineStage::isLost)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No 'Lost' stage found in this pipeline"));

        opp.setStage(lostStage);
        opp.setActualCloseDate(LocalDate.now());
        opp.setProbabilityPct(0);
        opp.setUpdatedBy(actor.getId());
        Opportunity saved = opportunityRepository.save(opp);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "DEAL_LOST", "OPPORTUNITY", saved.getId(), saved.getTitle(),
                null, Map.of("value", saved.getValue()),
                null, null);

        // Publish event for notifications
        eventPublisher.publishEvent(new DealLostEvent(
                this, saved.getId(), workspaceId, actor.getId(), actorEmail,
                saved.getTitle(), saved.getValue()
        ));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Opportunity> listByStage(Long workspaceId, Long stageId) {
        return opportunityRepository.findByWorkspaceIdAndStageIdOrderByCreatedAtDesc(workspaceId, stageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Opportunity> listOpen(Long workspaceId) {
        return opportunityRepository.findOpenOpportunities(workspaceId);
    }
}
