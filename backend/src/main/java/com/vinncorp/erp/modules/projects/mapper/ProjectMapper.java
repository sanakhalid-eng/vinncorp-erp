package com.vinncorp.erp.modules.projects.mapper;
import com.vinncorp.erp.platform.user.mapper.UserMapper;
import com.vinncorp.erp.modules.projects.dto.response.ProjectMemberResponse;
import com.vinncorp.erp.modules.projects.dto.response.ProjectResponse;
import com.vinncorp.erp.modules.projects.entity.Project;

import java.util.List;

public class ProjectMapper {

    public static ProjectResponse toResponse(Project project) {

        if (project == null) return null;

        ProjectResponse response = new ProjectResponse();

        response.setId(project.getId());
        response.setWorkspaceId(project.getWorkspace() != null ? project.getWorkspace().getId() : null);
        response.setWorkspaceName(project.getWorkspace() != null ? project.getWorkspace().getName() : null);
        response.setName(project.getName());
        response.setDescription(project.getDescription());

        // -------------------------
        // STATUS (NOW CLEAN & CORRECT)
        // -------------------------
        if (project.getStatus() != null) {
            response.setStatusId(project.getStatus().getId());
            response.setStatusName(project.getStatus().getName());
        }

        response.setCreatedAt(project.getCreatedAt());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());

        // -------------------------
        // NEW ENHANCED FIELDS
        // -------------------------
        response.setPriority(project.getPriority());
        response.setTags(project.getTags());
        response.setCategory(project.getCategory());
        response.setObjectives(project.getObjectives());
        response.setBudget(project.getBudget());
        response.setCurrency(project.getCurrency());
        response.setActive(project.isActive());
        response.setPublic(project.isPublic());

        // -------------------------
        // OWNER
        // -------------------------
        response.setOwner(
                project.getOwner() != null
                        ? UserMapper.toResponse(project.getOwner())
                        : null
        );

        // -------------------------
        // PROJECT MANAGER
        // -------------------------
        response.setProjectManager(
                project.getProjectManager() != null
                        ? UserMapper.toResponse(project.getProjectManager())
                        : null
        );

        // -------------------------
        // MEMBERS
        // -------------------------
        List<ProjectMemberResponse> members =
                project.getMembers() == null
                        ? List.of()
                        : project.getMembers()
                        .stream()
                        .map(ProjectMemberMapper::toResponse)
                        .toList();

        response.setMembers(members);
        response.setMemberCount(members.size());

        return response;
    }
}


