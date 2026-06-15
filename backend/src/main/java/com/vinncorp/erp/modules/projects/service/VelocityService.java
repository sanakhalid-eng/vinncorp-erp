package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.VelocityResponse;
import com.vinncorp.erp.modules.projects.dto.response.VelocityTrendResponse;

public interface VelocityService {

    VelocityResponse getSprintVelocity(Long sprintId);

    VelocityTrendResponse getProjectVelocityHistory(Long projectId);

    void generateVelocitySnapshot(Long sprintId);

    void evictCache(Long sprintId);
}



