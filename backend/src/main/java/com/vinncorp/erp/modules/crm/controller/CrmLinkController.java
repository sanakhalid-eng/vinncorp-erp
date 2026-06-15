package com.vinncorp.erp.modules.crm.controller;

import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.crm.entity.Opportunity;
import com.vinncorp.erp.modules.crm.repository.OpportunityRepository;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/crm")
@RequiredArgsConstructor
@Tag(name = "CRM ↔ Project Links")
@PreAuthorize("isAuthenticated()")
public class CrmLinkController {

    private final OpportunityRepository opportunityRepository;
    private final ProjectRepository projectRepository;
    private final CurrentWorkspaceResolver workspaceResolver;

    @GetMapping("/opportunities/{id}/project")
    @Operation(summary = "Get the project linked to an opportunity (via converted lead)")
    public ResponseEntity<Map<String, Object>> getProjectByOpportunity(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();

        Optional<Opportunity> oppOpt = opportunityRepository.findByIdAndWorkspaceId(id, wsId);
        if (oppOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Opportunity opp = oppOpt.get();

        // Find project: try by exact name match in workspace, then by category
        Optional<Project> projectOpt = projectRepository.findByNameAndWorkspaceIdAndDeletedAtIsNull(opp.getTitle(), wsId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("opportunityId", opp.getId());
        result.put("opportunityTitle", opp.getTitle());

        if (projectOpt.isPresent()) {
            Project p = projectOpt.get();
            result.put("projectId", p.getId());
            result.put("projectName", p.getName());
            result.put("projectCategory", p.getCategory());
            result.put("budget", p.getBudget());
            result.put("currency", p.getCurrency());
            result.put("priority", p.getPriority() != null ? p.getPriority().name() : null);
            result.put("active", p.isActive());
            result.put("linked", true);
        } else {
            // Try by category "CRM Conversion" and similar name prefix
            List<Project> crmProjects = projectRepository.findByWorkspaceIdAndCategoryAndDeletedAtIsNull(wsId, "CRM Conversion");
            Optional<Project> match = crmProjects.stream()
                    .filter(p -> opp.getTitle() != null && p.getName() != null && p.getName().contains(opp.getTitle()))
                    .findFirst();
            if (match.isPresent()) {
                Project p = match.get();
                result.put("projectId", p.getId());
                result.put("projectName", p.getName());
                result.put("projectCategory", p.getCategory());
                result.put("budget", p.getBudget());
                result.put("currency", p.getCurrency());
                result.put("priority", p.getPriority() != null ? p.getPriority().name() : null);
                result.put("active", p.isActive());
                result.put("linked", true);
            } else {
                result.put("linked", false);
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers/{id}/projects")
    @Operation(summary = "Get all projects linked to a customer via won opportunities")
    public ResponseEntity<List<Map<String, Object>>> getProjectsByCustomer(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();

        // Find all won opportunities for this customer
        List<Opportunity> wonOpps = opportunityRepository.findByCustomerIdAndWorkspaceIdOrderByCreatedAtDesc(wsId, id)
                .stream()
                .filter(o -> o.getStage() != null && o.getStage().isWon())
                .toList();

        List<Map<String, Object>> projects = new ArrayList<>();

        for (Opportunity opp : wonOpps) {
            Optional<Project> projectOpt = projectRepository.findByNameAndWorkspaceIdAndDeletedAtIsNull(opp.getTitle(), wsId);
            if (projectOpt.isEmpty()) {
                List<Project> crmProjects = projectRepository.findByWorkspaceIdAndCategoryAndDeletedAtIsNull(wsId, "CRM Conversion");
                projectOpt = crmProjects.stream()
                        .filter(p -> opp.getTitle() != null && p.getName() != null && p.getName().contains(opp.getTitle()))
                        .findFirst();
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("opportunityId", opp.getId());
            entry.put("opportunityTitle", opp.getTitle());
            entry.put("opportunityValue", opp.getValue());

            if (projectOpt.isPresent()) {
                Project p = projectOpt.get();
                entry.put("projectId", p.getId());
                entry.put("projectName", p.getName());
                entry.put("projectCategory", p.getCategory());
                entry.put("budget", p.getBudget());
                entry.put("currency", p.getCurrency());
                entry.put("priority", p.getPriority() != null ? p.getPriority().name() : null);
                entry.put("active", p.isActive());
                entry.put("linked", true);
            } else {
                entry.put("linked", false);
            }

            projects.add(entry);
        }

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/leads/{id}/opportunities")
    @Operation(summary = "Get all opportunities linked to a lead")
    public ResponseEntity<List<Opportunity>> getOpportunitiesByLead(@PathVariable Long id) {
        Long wsId = workspaceResolver.getCurrentWorkspaceId();
        List<Opportunity> opps = opportunityRepository.findByLeadIdAndWorkspaceIdOrderByCreatedAtDesc(id, wsId);
        return ResponseEntity.ok(opps);
    }
}
