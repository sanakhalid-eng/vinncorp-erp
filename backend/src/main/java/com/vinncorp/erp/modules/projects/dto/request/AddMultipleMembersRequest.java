package com.vinncorp.erp.modules.projects.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AddMultipleMembersRequest {
    private List<AddProjectMemberRequest> members;
}



