package com.vinncorp.erp.modules.crm.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.crm.entity.Customer;
import com.vinncorp.erp.modules.crm.entity.Opportunity;
import com.vinncorp.erp.modules.crm.service.CrmProjectIntegrationService;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.enums.ProjectPriority;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrmProjectIntegrationServiceImpl implements CrmProjectIntegrationService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    @Transactional
    public Project createProjectFromOpportunity(Opportunity opportunity, Customer customer, String actorEmail) {
        try {
            User actor = userRepository.findByEmail(actorEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));
            Workspace workspace = workspaceRepository.findById(opportunity.getWorkspaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

            // Create project from opportunity
            Project project = new Project();
            project.setName(opportunity.getTitle());
            project.setDescription(buildProjectDescription(opportunity, customer));
            project.setWorkspace(workspace);
            project.setOwner(actor);
            project.setPriority(ProjectPriority.MEDIUM);
            project.setBudget(opportunity.getValue() != null ? opportunity.getValue().doubleValue() : null);
            project.setCurrency(opportunity.getCurrency());
            project.setCategory("CRM Conversion");
            project.setStartDate(LocalDateTime.now());
            if (opportunity.getExpectedCloseDate() != null) {
                project.setEndDate(opportunity.getExpectedCloseDate().atStartOfDay());
            }
            project.setCreatedBy(actor.getId());
            project.setUpdatedBy(actor.getId());

            Project saved = projectRepository.save(project);
            log.info("Project created from opportunity {}: {}", opportunity.getId(), saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create project from opportunity {}: {}", opportunity.getId(), e.getMessage());
            return null;
        }
    }

    @Override
    public Long resolveWorkspaceId(Long workspaceId) {
        return workspaceId;
    }

    private String buildProjectDescription(Opportunity opportunity, Customer customer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Auto-created from CRM Opportunity: ").append(opportunity.getTitle());
        if (customer != null) {
            sb.append("\n\nCustomer: ").append(customer.getName());
            if (customer.getIndustry() != null) {
                sb.append("\nIndustry: ").append(customer.getIndustry());
            }
        }
        sb.append("\nDeal Value: ").append(opportunity.getValue()).append(" ").append(opportunity.getCurrency());
        if (opportunity.getNotes() != null && !opportunity.getNotes().isBlank()) {
            sb.append("\n\nNotes: ").append(opportunity.getNotes());
        }
        return sb.toString();
    }
}
