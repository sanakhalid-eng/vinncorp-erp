package com.vinncorp.erp.modules.finance.service;

import com.vinncorp.erp.modules.finance.dto.response.FinanceDashboardResponse;

public interface FinanceDashboardService {

    FinanceDashboardResponse getDashboard(Long workspaceId);
}
