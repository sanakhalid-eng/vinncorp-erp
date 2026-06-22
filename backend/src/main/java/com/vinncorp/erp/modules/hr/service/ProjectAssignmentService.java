package com.vinncorp.erp.modules.hr.service;

import com.vinncorp.erp.modules.hr.dto.request.ProjectAssignmentRequest;
import com.vinncorp.erp.modules.hr.dto.response.ProjectAssignmentResponse;

import java.util.List;

public interface ProjectAssignmentService {

    ProjectAssignmentResponse assign(ProjectAssignmentRequest request, Long workspaceId, String actorEmail);

    ProjectAssignmentResponse update(Long id, ProjectAssignmentRequest request, Long workspaceId, String actorEmail);

    ProjectAssignmentResponse unassign(Long id, Long workspaceId, String actorEmail);

    ProjectAssignmentResponse get(Long id, Long workspaceId);

    List<ProjectAssignmentResponse> getByEmployee(Long employeeId, Long workspaceId);

    List<ProjectAssignmentResponse> getByProject(Long projectId, Long workspaceId);

    List<ProjectAssignmentResponse> getAll(Long workspaceId);
}
