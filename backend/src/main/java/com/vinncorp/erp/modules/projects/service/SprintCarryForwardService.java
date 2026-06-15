package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;

import java.util.List;

public interface SprintCarryForwardService {

    List<TaskResponse> getCarryForwardCandidates(Long sprintId);

    List<TaskResponse> getDependencyAwareRollover(Long sprintId);

    List<TaskResponse> getBlockedTaskPriorities(Long sprintId);
}



