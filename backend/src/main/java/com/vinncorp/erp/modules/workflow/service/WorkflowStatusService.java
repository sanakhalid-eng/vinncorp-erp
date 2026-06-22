package com.vinncorp.erp.modules.workflow.service;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowStatusOrderRequest;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowStatusRequest;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowStatusResponse;
import java.util.List;

public interface WorkflowStatusService {
WorkflowStatusResponse createStatus(Long projectId, WorkflowStatusRequest request);
WorkflowStatusResponse updateStatus(Long projectId, Long statusId, WorkflowStatusRequest request);
List<WorkflowStatusResponse> getStatuses(Long projectId);
List<WorkflowStatusResponse> reorderStatuses(Long projectId, List<WorkflowStatusOrderRequest> request);
void deleteStatus(Long statusId);
} 