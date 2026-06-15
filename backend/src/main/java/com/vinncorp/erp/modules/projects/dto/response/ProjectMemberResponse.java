package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.core.user.entity.UserSummary;
import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.modules.projects.entity.ProjectRole;

import lombok.Data;

@Data
public class ProjectMemberResponse {

    private Long id;

    private Role role;

    private ProjectRole projectRole;

    private UserSummary user;
}


