package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.DashboardSummaryResponse;
import com.vinncorp.erp.modules.projects.dto.response.EmployeeDashboardResponse;

public interface DashboardService {

    DashboardSummaryResponse getDashboardSummary(String email);

    EmployeeDashboardResponse getEmployeeDashboard(String email);
}



