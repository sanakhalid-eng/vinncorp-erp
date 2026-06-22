package com.vinncorp.erp.modules.workflow.service;
import com.vinncorp.erp.modules.workflow.dto.request.WorkflowTransitionRequest;
import com.vinncorp.erp.modules.workflow.dto.response.WorkflowTransitionResponse;
import java.util.List;

public interface WorkflowTransitionService {
WorkflowTransitionResponse createTransition(Long projectId, WorkflowTransitionRequest request);
List<WorkflowTransitionResponse> getTransitions(Long projectId);
void deleteTransition(Long projectId, Long transitionId);
} 