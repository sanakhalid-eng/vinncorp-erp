package com.vinncorp.erp.modules.projects.service.impl;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.user.entity.User;

import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.core.user.constants.PermissionConstants;
import com.vinncorp.erp.modules.projects.dto.request.CreateProjectRequest;
import com.vinncorp.erp.modules.projects.dto.response.ProjectResponse;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.enums.ProjectPriority;
import com.vinncorp.erp.modules.projects.mapper.ProjectMapper;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRoleRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowStatusRepository;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.modules.projects.service.PermissionService;
import com.vinncorp.erp.modules.projects.service.ProjectService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.cache.CacheNames;
import com.vinncorp.erp.shared.cache.CacheService;
import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkflowStatusRepository workflowStatusRepository;
    private final ActivityLogService activityLogService;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final PermissionService permissionService;
    private final CacheService cacheService;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String ownerEmail) {
        Long workspaceId = resolveWorkspaceId();
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspaceMemberRepository.findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, owner.getId())
                .orElseThrow(() -> new BadRequestException("User is not a member of this workspace"));

        Project project = new Project();
        project.setWorkspace(workspace);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setOwner(owner);
        project.setPriority(request.getPriority() != null ? request.getPriority() : ProjectPriority.MEDIUM);
        project.setTags(request.getTags());
        project.setCategory(request.getCategory());
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
        WorkflowStatus defaultStatus = createDefaultWorkflowStatuses(savedProject);
        savedProject.setStatus(defaultStatus);
        savedProject = projectRepository.save(savedProject);

        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(savedProject);
        ownerMember.setUser(owner);
        ProjectRole role = projectRoleRepository.findByName(PermissionConstants.PROJECT_MANAGER)
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
                "Project created: " + savedProject.getName(), savedProject.getId());
        return ProjectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public List<ProjectResponse> getAllProjects() {
        Long workspaceId = resolveWorkspaceId();
        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .map(ProjectMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public List<ProjectResponse> getProjectsForUser(String email) {
        Long workspaceId = resolveWorkspaceId();
        List<Project> owned = projectRepository.findByWorkspaceIdAndOwner_Email(workspaceId, email);
        List<Project> member = projectRepository.findDistinctByWorkspaceIdAndMembers_User_Email(workspaceId, email);
        return Stream.concat(owned.stream(), member.stream())
                .distinct()
                .map(ProjectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        validateWorkspaceAccess(project);
        return ProjectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, CreateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        validateWorkspaceAccess(project);

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("name", project.getName());
        oldValue.put("description", project.getDescription());
        oldValue.put("priority", project.getPriority());
        oldValue.put("tags", project.getTags());
        oldValue.put("category", project.getCategory());
        oldValue.put("objectives", project.getObjectives());
        oldValue.put("budget", project.getBudget());
        oldValue.put("currency", project.getCurrency());
        oldValue.put("isActive", project.isActive());
        oldValue.put("isPublic", project.isPublic());

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        if (request.getPriority() != null) project.setPriority(request.getPriority());
        if (request.getTags() != null) project.setTags(request.getTags());
        if (request.getCategory() != null) project.setCategory(request.getCategory());
        if (request.getObjectives() != null) project.setObjectives(request.getObjectives());
        if (request.getBudget() != null) project.setBudget(request.getBudget());
        if (request.getCurrency() != null) project.setCurrency(request.getCurrency());
        if (request.getIsActive() != null) project.setActive(request.getIsActive());
        if (request.getIsPublic() != null) project.setPublic(request.getIsPublic());
        if (request.getProjectManagerId() != null) {
            User projectManager = userRepository.findById(request.getProjectManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project manager not found"));
            project.setProjectManager(projectManager);
        }

        Project updatedProject = projectRepository.save(project);
        Map<String, Object> newValue = new HashMap<>();
        newValue.put("name", updatedProject.getName());
        newValue.put("description", updatedProject.getDescription());
        newValue.put("priority", updatedProject.getPriority());
        newValue.put("tags", updatedProject.getTags());
        newValue.put("category", updatedProject.getCategory());
        newValue.put("objectives", updatedProject.getObjectives());
        newValue.put("budget", updatedProject.getBudget());
        newValue.put("currency", updatedProject.getCurrency());
        newValue.put("isActive", updatedProject.isActive());
        newValue.put("isPublic", updatedProject.isPublic());

        detectProjectChanges(project.getId(), oldValue, newValue);
        return ProjectMapper.toResponse(updatedProject);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        validateWorkspaceAccess(project);

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("id", project.getId());
        oldValue.put("name", project.getName());

        Long workspaceId = resolveWorkspaceId();
        permissionService.evictProjectPermissionsInWorkspace(project.getId());
        cacheService.evict(CacheNames.projects(workspaceId));
        cacheService.evict(CacheNames.dashboard(workspaceId));

        activityLogService.logActivity(
                project.getOwner() != null ? project.getOwner().getId() : null,
                EntityType.PROJECT, project.getId(), ActionType.DELETED,
                oldValue, null, "Project deleted: " + project.getName(), project.getId());
        Long deletedBy = getCurrentUserId();
        project.softDelete(deletedBy);
        projectRepository.save(project);
    }

    private void validateWorkspaceAccess(Project project) {
        Long workspaceId = resolveWorkspaceId();
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the current workspace");
        }
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

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            String email = auth.getName();
            return userRepository.findByEmail(email).map(User::getId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private WorkflowStatus createDefaultWorkflowStatuses(Project project) {
        WorkflowStatus todo = new WorkflowStatus();
        todo.setName("TODO"); todo.setColor("#f59e0b"); todo.setOrderIndex(0); todo.setDefault(true); todo.setProject(project);
        WorkflowStatus inProgress = new WorkflowStatus();
        inProgress.setName("IN_PROGRESS"); inProgress.setColor("#3b82f6"); inProgress.setOrderIndex(1); inProgress.setDefault(false); inProgress.setProject(project);
        WorkflowStatus done = new WorkflowStatus();
        done.setName("DONE"); done.setColor("#10b981"); done.setOrderIndex(2); done.setDefault(false); done.setProject(project);
        workflowStatusRepository.saveAll(List.of(todo, inProgress, done));
        return todo;
    }

    private void detectProjectChanges(Long projectId, Map<String, Object> oldValue, Map<String, Object> newValue) {
        if (!java.util.Objects.equals(oldValue.get("name"), newValue.get("name"))) {
            activityLogService.logActivity(null, EntityType.PROJECT, projectId, ActionType.UPDATED,
                    Map.of("name", oldValue.get("name")), Map.of("name", newValue.get("name")),
                    "Project name changed", projectId);
        }
        if (!java.util.Objects.equals(oldValue.get("priority"), newValue.get("priority"))) {
            activityLogService.logActivity(null, EntityType.PROJECT, projectId, ActionType.PRIORITY_CHANGED,
                    Map.of("priority", oldValue.get("priority")), Map.of("priority", newValue.get("priority")),
                    "Priority changed from " + oldValue.get("priority") + " to " + newValue.get("priority"), projectId);
        }
        if (!java.util.Objects.equals(oldValue.get("isActive"), newValue.get("isActive"))) {
            activityLogService.logActivity(null, EntityType.PROJECT, projectId, ActionType.UPDATED,
                    Map.of("isActive", oldValue.get("isActive")), Map.of("isActive", newValue.get("isActive")),
                    "Project active status changed", projectId);
        }
    }
}



