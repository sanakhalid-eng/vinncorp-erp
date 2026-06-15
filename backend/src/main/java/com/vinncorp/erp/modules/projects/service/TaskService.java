package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.SubtaskRequest;
import com.vinncorp.erp.modules.projects.dto.request.TaskFilterRequest;
import com.vinncorp.erp.modules.projects.dto.request.TaskRequest;
import com.vinncorp.erp.modules.projects.dto.response.BlockedStatusResponse;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import com.vinncorp.erp.modules.projects.dto.response.SubtaskProgressResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskResponse;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    TaskResponse createTask(TaskRequest request, String email);

    List<TaskResponse> getAllTasksForUser(String email);

    TaskResponse getTaskById(Long id);

    TaskResponse updateTask(Long id, TaskRequest request, String email);

    void deleteTask(Long id);

    PaginatedResponse<TaskResponse> getTasksByProjectWithFilter(Long projectId, TaskFilterRequest filter, int page, int size);

    PaginatedResponse<TaskResponse> getMyTasks(String email,  int page, int size);

    @Transactional
    TaskResponse updateTaskStatus(Long taskId, Long newStatusId, String email);

    PaginatedResponse<TaskResponse> getTasksByAssigneeInProject(Long projectId, Long userId, int page, int size);

    PaginatedResponse<TaskResponse> getTasksByProject(Long projectId, Long status, String priority, String search, Long assigneeId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    TaskResponse moveTask(Long taskId, Long sourceColumnId, Long targetColumnId, Integer position, String email);

    TaskResponse createSubtask(Long parentTaskId, SubtaskRequest request, String email);

    List<TaskResponse> getSubtasks(Long parentTaskId);

    SubtaskProgressResponse getSubtaskProgress(Long parentTaskId);

    TaskResponse updateSubtaskParent(Long taskId, Long newParentTaskId, String email);

    BlockedStatusResponse isTaskBlocked(Long taskId);

    TaskResponse toggleSubtaskCompletion(Long subtaskId, String email);

    TaskResponse updateSubtask(Long subtaskId, SubtaskRequest request, String email);

    TaskResponse cloneTask(Long taskId, String email);
}


