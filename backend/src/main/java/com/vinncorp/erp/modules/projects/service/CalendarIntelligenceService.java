package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.CalendarIntelligenceResponse;

public interface CalendarIntelligenceService {

    CalendarIntelligenceResponse analyze(Long workspaceId, Long projectId);
}



