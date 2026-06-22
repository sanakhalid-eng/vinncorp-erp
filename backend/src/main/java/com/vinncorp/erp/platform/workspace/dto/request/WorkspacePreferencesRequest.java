package com.vinncorp.erp.platform.workspace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspacePreferencesRequest {
    private String timezone;
    private String dateFormat;
    private String weekStartDay;
    private String defaultDashboardView;
}

