package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.PersonalProductivityDashboardResponse;

public interface PersonalProductivityService {

    PersonalProductivityDashboardResponse getDashboard(Long workspaceId, Long userId);
}



