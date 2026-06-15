package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.SprintHealthResponse;

public interface SprintHealthService {

    SprintHealthResponse assessHealth(Long sprintId);

    void evictCache(Long sprintId);
}



