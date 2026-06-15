package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.BlockedStatusResponse;
import com.vinncorp.erp.modules.projects.dto.response.DependencyGraphResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskDependencyResponse;
import com.vinncorp.erp.modules.projects.enums.DependencyType;

import java.util.List;

public interface TaskDependencyService {

    TaskDependencyResponse addDependency(Long taskId, Long dependsOnTaskId, DependencyType type, String description, String email);

    void removeDependency(Long taskId, Long dependsOnTaskId, String email);

    List<TaskDependencyResponse> getDependencies(Long taskId);

    List<TaskDependencyResponse> getBlockingTasks(Long taskId);

    List<TaskDependencyResponse> getRelatedTasks(Long taskId);

    void validateDependenciesBeforeStatusChange(Long taskId);

    BlockedStatusResponse getBlockedStatus(Long taskId);

    DependencyGraphResponse getDependencyGraph(Long taskId);

    void evictDependencyCaches(Long taskId);

    void evictRelatedDependencyCaches(Long taskId);
}



