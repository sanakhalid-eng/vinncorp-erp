package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryResponse {

    private long totalProjects;
    private long totalTasks;
    private long myTasks;
    private long completedTasks;
    private long overdueTasks;
    private long dueSoonTasks;
    private Map<String, Long> tasksByPriority;
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> roleCounts;
    private List<DashboardTaskItem> recentTasks;
    private List<DashboardProjectItem> recentProjects;
    private List<DashboardMemberItem> membersOverview;

    @Data
    @Builder
    public static class DashboardTaskItem {
        private Long id;
        private String title;
        private String status;
        private String priority;
        private Long projectId;
        private String projectName;
        private LocalDateTime updatedAt;
        private LocalDateTime dueDate;
    }

    @Data
    @Builder
    public static class DashboardProjectItem {
        private Long id;
        private String name;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime endDate;
        private int memberCount;
    }

    @Data
    @Builder
    public static class DashboardMemberItem {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String avatarUrl;
    }
}



