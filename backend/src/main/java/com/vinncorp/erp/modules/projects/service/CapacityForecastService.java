package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.CapacityForecastResponse;

public interface CapacityForecastService {

    CapacityForecastResponse forecastForSprint(Long workspaceId, Long sprintId);
}



