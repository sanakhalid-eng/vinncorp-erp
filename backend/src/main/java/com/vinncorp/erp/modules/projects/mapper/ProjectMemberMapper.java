package com.vinncorp.erp.modules.projects.mapper;

import com.vinncorp.erp.core.user.entity.UserSummary;
import com.vinncorp.erp.modules.projects.dto.response.ProjectMemberResponse;
import com.vinncorp.erp.modules.projects.entity.ProjectMember;

public class ProjectMemberMapper {

    public static ProjectMemberResponse toResponse(ProjectMember member) {

        if (member == null) return null;

        ProjectMemberResponse response = new ProjectMemberResponse();

        response.setId(member.getId());
        response.setRole(member.getRole());
        response.setProjectRole(member.getProjectRole());

        UserSummary user = new UserSummary();
        user.setId(member.getUser().getId());
        user.setName(member.getUser().getName());
        user.setEmail(member.getUser().getEmail());
        user.setAvatarUrl(member.getUser().getAvatarUrl());

        response.setUser(user);

        return response;
    }
}



