package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.WorkflowTransitionRequest;
import com.vinncorp.erp.modules.projects.dto.response.WorkflowTransitionResponse;

import java.util.List;

public interface WorkflowTransitionService {

    WorkflowTransitionResponse createTransition(Long projectId, WorkflowTransitionRequest request);

    List<WorkflowTransitionResponse> getTransitions(Long projectId);

    void deleteTransition(Long projectId, Long transitionId);
}



