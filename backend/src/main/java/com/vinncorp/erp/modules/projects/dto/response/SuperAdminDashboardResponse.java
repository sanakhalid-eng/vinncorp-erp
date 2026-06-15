package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SuperAdminDashboardResponse {

    private long totalWorkspaces;
    private long totalUsers;
    private long totalEmployees;
    private long totalProjects;
    private long activeWorkspaces;
    private List<WorkspaceSummaryItem> recentWorkspaces;
    private List<UserSummaryItem> recentUsers;

    @Data
    @Builder
    public static class WorkspaceSummaryItem {
        private Long id;
        private String name;
        private String slug;
        private boolean active;
        private int memberCount;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class UserSummaryItem {
        private Long id;
        private String name;
        private String email;
        private String avatarUrl;
        private boolean isActive;
        private LocalDateTime createdAt;
    }
}
