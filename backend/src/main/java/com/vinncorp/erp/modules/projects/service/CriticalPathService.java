package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.CriticalPathResponse;
import com.vinncorp.erp.modules.projects.dto.response.CriticalTaskResponse;
import com.vinncorp.erp.modules.projects.dto.response.DeliveryRiskResponse;
import com.vinncorp.erp.modules.projects.dto.response.DependencyImpactResponse;

import java.util.List;

public interface CriticalPathService {

    CriticalPathResponse getCriticalPath(Long workspaceId, Long projectId);

    CriticalTaskResponse getTaskCriticality(Long workspaceId, Long taskId);

    List<DeliveryRiskResponse> getDeliveryRisks(Long workspaceId, Long projectId);

    DependencyImpactResponse getDependencyImpact(Long workspaceId, Long taskId);
}



