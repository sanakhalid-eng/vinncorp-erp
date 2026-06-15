package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.SprintRequest;
import com.vinncorp.erp.modules.projects.dto.response.SprintResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;

import java.util.List;

public interface SprintService {

    SprintResponse createSprint(SprintRequest request, String email);

    SprintResponse startSprint(Long sprintId, String email);

    SprintResponse completeSprint(Long sprintId, String email, boolean carryForward);

    SprintResponse completeSprint(Long sprintId, String email);

    List<SprintResponse> getProjectSprints(Long projectId);

    SprintResponse getActiveSprint(Long projectId);

    SprintResponse getSprintById(Long sprintId);

    List<TaskResponse> getBacklogTasks(Long projectId);

    void deleteSprint(Long sprintId, String email);
}



