package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.MonteCarloForecastResponse;

public interface MonteCarloForecastService {

    MonteCarloForecastResponse forecast(Long workspaceId, Long sprintId);
}



