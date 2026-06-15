package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.WorkloadBalanceResponse;

public interface WorkloadBalancingService {

    WorkloadBalanceResponse analyze(Long sprintId);

    void evictCache(Long sprintId);
}



