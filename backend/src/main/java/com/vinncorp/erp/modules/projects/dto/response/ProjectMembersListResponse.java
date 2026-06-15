package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ProjectMembersListResponse {
    private List<ProjectMemberResponse> members;
    private int totalMembers;
}



