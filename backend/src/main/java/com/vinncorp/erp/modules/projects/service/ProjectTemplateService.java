package com.vinncorp.erp.modules.projects.service;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.user.entity.User;

import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceMemberRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.modules.projects.dto.request.CreateProjectFromTemplateRequest;
import com.vinncorp.erp.modules.projects.dto.request.SaveProjectAsTemplateRequest;
import com.vinncorp.erp.modules.projects.dto.response.ProjectResponse;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.enums.ProjectPriority;
import com.vinncorp.erp.modules.projects.mapper.ProjectMapper;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectTemplateService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final ProjectTemplateRepository projectTemplateRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ActivityLogService activityLogService;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final ObjectMapper objectMapper;

    public List<ProjectTemplate> getTemplates() {
        return projectTemplateRepository.findByIsActiveTrue().stream()
                .map(this::toProjectTemplate)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse createProjectFromTemplate(Long templateId, CreateProjectFromTemplateRequest request, String ownerEmail) {
        ProjectTemplateEntity template = projectTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        List<String> columns = parseJsonList(template.getDefaultColumns());
        List<String> labels = parseJsonList(template.getDefaultLabels());

        Long workspaceId = resolveWorkspaceId();

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspaceMemberRepository.findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, owner.getId())
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        Project project = new Project();
        project.setWorkspace(workspace);
        project.setName(request.getName() != null ? request.getName() : template.getName());
        project.setDescription(request.getDescription() != null ? request.getDescription() : template.getDescription());
        project.setOwner(owner);
        project.setPriority(request.getPriority() != null ? ProjectPriority.valueOf(request.getPriority()) : ProjectPriority.MEDIUM);
        project.setTags(request.getTags());
        project.setCategory(request.getCategory() != null ? request.getCategory() : template.getCategory());
        project.setObjectives(request.getObjectives());
        project.setBudget(request.getBudget());
        project.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        project.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        project.setPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        if (request.getProjectManagerId() != null) {
            User projectManager = userRepository.findById(request.getProjectManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project manager not found"));
            project.setProjectManager(projectManager);
        }

        Project savedProject = projectRepository.save(project);

        List<WorkflowStatus> statuses = createWorkflowStatuses(savedProject, columns);
        savedProject.setStatus(statuses.isEmpty() ? null : statuses.getFirst());
        savedProject = projectRepository.save(savedProject);

        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(savedProject);
        ownerMember.setUser(owner);
        ProjectRole role = projectRoleRepository.findByName("PROJECT_MANAGER")
                .orElseThrow(() -> new ResourceNotFoundException("Default project role not found"));
        ownerMember.setProjectRole(role);
        projectMemberRepository.save(ownerMember);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("name", savedProject.getName());
        newValue.put("description", savedProject.getDescription());
        newValue.put("priority", savedProject.getPriority());
        newValue.put("category", savedProject.getCategory());
        activityLogService.logActivity(
                owner.getId(), EntityType.PROJECT, savedProject.getId(),
                ActionType.CREATED, null, newValue,
                "Project created from template: " + template.getName(), savedProject.getId());

        return ProjectMapper.toResponse(savedProject);
    }

    @Transactional
    public ProjectTemplate saveProjectAsTemplate(Long projectId, @Valid SaveProjectAsTemplateRequest request, String ownerEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Long workspaceId = resolveWorkspaceId();
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the current workspace");
        }

        List<WorkflowStatus> statuses = workflowStatusRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
        List<String> columnNames = statuses.stream()
                .map(WorkflowStatus::getName)
                .collect(Collectors.toList());

        List<String> labels = new ArrayList<>();
        if (project.getTags() != null && !project.getTags().isBlank()) {
            labels = Arrays.stream(project.getTags().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        ProjectTemplateEntity templateEntity =
                new ProjectTemplateEntity();
        templateEntity.setName(request.getName());
        templateEntity.setDescription(request.getDescription());
        templateEntity.setCategory(project.getCategory());
        templateEntity.setIcon("Sparkles");
        templateEntity.setHasSprints(false);
        templateEntity.setDefaultLabels(toJson(labels));
        templateEntity.setDefaultColumns(toJson(columnNames));
        templateEntity.setActive(true);

        templateEntity = projectTemplateRepository.save(templateEntity);

        return toProjectTemplate(templateEntity);
    }

    private List<WorkflowStatus> createWorkflowStatuses(Project project, List<String> columnNames) {
        if (columnNames == null || columnNames.isEmpty()) {
            columnNames = List.of("TODO", "IN_PROGRESS", "DONE");
        }
        List<WorkflowStatus> statuses = new ArrayList<>();
        String[] colors = {"#f59e0b", "#3b82f6", "#8b5cf6", "#10b981", "#ef4444", "#ec4899", "#f97316", "#14b8a6", "#6366f1", "#84cc16"};
        for (int i = 0; i < columnNames.size(); i++) {
            WorkflowStatus status = new WorkflowStatus();
            status.setName(columnNames.get(i));
            status.setColor(colors[i % colors.length]);
            status.setOrderIndex(i);
            status.setDefault(i == 0);
            status.setProject(project);
            status.setPosition(i);
            statuses.add(status);
        }
        return workflowStatusRepository.saveAll(statuses);
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private ProjectTemplate toProjectTemplate(ProjectTemplateEntity entity) {
        return ProjectTemplate.builder()
                .id(String.valueOf(entity.getId()))
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .icon(entity.getIcon())
                .hasSprints(entity.isHasSprints())
                .defaultLabels(parseJsonList(entity.getDefaultLabels()))
                .defaultColumns(parseJsonList(entity.getDefaultColumns()))
                .build();
    }

    private Long resolveWorkspaceId() {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        if (workspaceId == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                workspaceId = currentWorkspaceResolver.resolveDefaultWorkspace(userDetails.getUserId())
                        .map(Workspace::getId)
                        .orElseThrow(() -> new BadRequestException("No workspace context available"));
            } else {
                throw new BadRequestException("No workspace context available");
            }
        }
        return workspaceId;
    }
}



