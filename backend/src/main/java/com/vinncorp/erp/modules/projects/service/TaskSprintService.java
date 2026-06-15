package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;

import java.util.List;

public interface TaskSprintService {

    void assignTaskToSprint(Long taskId, Long sprintId, String email);

    void removeTaskFromSprint(Long taskId, String email);

    List<TaskResponse> getSprintTasks(Long sprintId);

    Long getTaskSprintId(Long taskId);
}



