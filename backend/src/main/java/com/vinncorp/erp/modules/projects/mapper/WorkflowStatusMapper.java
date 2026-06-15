package com.vinncorp.erp.modules.projects.mapper;

import com.vinncorp.erp.modules.projects.dto.response.WorkflowStatusResponse;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;

public class WorkflowStatusMapper {

    public static WorkflowStatusResponse toResponse(WorkflowStatus status) {
        WorkflowStatusResponse res = new WorkflowStatusResponse();
        res.setId(status.getId());
        res.setName(status.getName());
        res.setColor(status.getColor());
        res.setOrderIndex(status.getOrderIndex());
        return res;
    }
}


