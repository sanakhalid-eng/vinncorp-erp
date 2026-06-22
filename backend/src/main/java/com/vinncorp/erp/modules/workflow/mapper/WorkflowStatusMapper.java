package com.vinncorp.erp.modules.workflow.mapper;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowStatusResponse;
import com.vinncorp.erp.modules.workflow.entity.WorkflowStatus;

public class WorkflowStatusMapper {

public static WorkflowStatusResponse toResponse(WorkflowStatus status) {
WorkflowStatusResponse res = new WorkflowStatusResponse();
res.setId(status.getId());
res.setName(status.getName());
res.setColor(status.getColor());
res.setOrderIndex(status.getOrderIndex());
return res;
}} 