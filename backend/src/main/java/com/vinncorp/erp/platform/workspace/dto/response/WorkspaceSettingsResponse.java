package com.vinncorp.erp.platform.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceSettingsResponse {
    private String workspaceName;
    private String slug;
    private WorkspaceFeatures features;
    private WorkspaceLimits limits;
    private WorkspacePreferences preferences;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkspaceFeatures {
        private boolean slackEnabled;
        private boolean webhooksEnabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkspaceLimits {
        private int maxProjects;
        private int maxMembers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkspacePreferences {
        private String timezone;
        private String dateFormat;
        private String weekStartDay;
        private String defaultDashboardView;
    }
}

