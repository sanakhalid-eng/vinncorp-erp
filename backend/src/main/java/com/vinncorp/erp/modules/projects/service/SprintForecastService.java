package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.SprintForecastResponse;

public interface SprintForecastService {

    SprintForecastResponse forecast(Long sprintId);

    void evictCache(Long sprintId);
}



