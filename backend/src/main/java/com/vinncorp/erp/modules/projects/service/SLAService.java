package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.SLARequest;
import com.vinncorp.erp.modules.projects.dto.response.SLABreachReportResponse;
import com.vinncorp.erp.modules.projects.dto.response.SLAResponse;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.enums.SLAType;

import java.util.List;

public interface SLAService {

    SLAResponse configureSLA(SLARequest request, String email);

    SLAResponse getTaskSLA(Long taskId, SLAType slaType);

    List<SLAResponse> getProjectSLAs(Long projectId);

    SLABreachReportResponse getSLAReport(Long projectId);

    void checkAndUpdateSLA(Long taskId);

    void startSLATimer(Task task, String email);

    void resolveSLA(Long taskId, SLAType slaType);

    long countActiveByWorkspace(Long workspaceId);

    long countBreachedByProject(Long projectId);
}



