package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.BurnDataPoint;
import com.vinncorp.erp.modules.projects.dto.response.BurndownDataPoint;

import java.util.List;

public interface SprintAnalyticsService {

    List<BurndownDataPoint> getBurndown(Long sprintId);

    List<BurnDataPoint> getBurnup(Long sprintId);

    void captureDailySnapshot(Long sprintId);

    void captureDailySnapshotsForAllActive();

    void evictCache(Long sprintId);
}



