package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.CapacityResponse;
import com.vinncorp.erp.modules.projects.dto.response.CapacitySummaryResponse;

import java.util.List;

public interface CapacityService {

    CapacityResponse setCapacity(Long sprintId, Long userId, double availableHours, int ptoDays, String email);

    CapacityResponse getMemberCapacity(Long sprintId, Long userId);

    List<CapacityResponse> getSprintCapacities(Long sprintId);

    CapacitySummaryResponse getCapacitySummary(Long sprintId);

    void recalculateAllocation(Long sprintId);

    void evictCache(Long sprintId);
}



