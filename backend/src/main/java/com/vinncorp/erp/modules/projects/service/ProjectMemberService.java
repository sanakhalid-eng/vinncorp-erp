package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.AddMultipleMembersRequest;
import com.vinncorp.erp.modules.projects.dto.request.AddProjectMemberRequest;
import com.vinncorp.erp.modules.projects.dto.request.ProjectMemberFilterRequest;
import com.vinncorp.erp.modules.projects.dto.request.UpdateProjectMemberRoleRequest;
import com.vinncorp.erp.modules.projects.dto.response.ProjectMemberResponse;

import java.util.List;

public interface ProjectMemberService {

    ProjectMemberResponse assignRole(Long projectId, Long userId, Long roleId);

    ProjectMemberResponse addMemberToProject(Long projectId, AddProjectMemberRequest request);

    List<ProjectMemberResponse> getProjectMembers(Long projectId);

    void removeMemberFromProject(Long projectId, Long userId);

    ProjectMemberResponse updateMemberRole(Long projectId, Long userId, UpdateProjectMemberRoleRequest request);

    ProjectMemberResponse getMember(Long projectId, Long userId);

    void leaveProject(Long projectId, String email);

    List<ProjectMemberResponse> addMultipleMembers(Long projectId, AddMultipleMembersRequest request);

    List<ProjectMemberResponse> filterMembers(Long projectId, ProjectMemberFilterRequest filter);

    ProjectMemberResponse inviteAssigneeByEmail(Long projectId, String email, Long inviterUserId);
}



