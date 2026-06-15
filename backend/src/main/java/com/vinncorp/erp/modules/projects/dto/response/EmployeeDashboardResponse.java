package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class EmployeeDashboardResponse {

    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String workEmail;
    private String jobTitle;
    private String departmentName;
    private String designationName;
    private LocalDate hireDate;
    private String employmentType;
    private String status;

    private long myProjectsCount;
    private long myTasksCount;
    private long completedTasks;
    private long pendingTasks;
    private long overdueTasks;

    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByPriority;

    private List<MyProjectItem> recentProjects;
    private List<MyTaskItem> recentTasks;

    @Data
    @Builder
    public static class MyProjectItem {
        private Long id;
        private String name;
        private String status;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class MyTaskItem {
        private Long id;
        private String title;
        private String status;
        private String priority;
        private String projectName;
        private LocalDateTime dueDate;
        private LocalDateTime updatedAt;
    }
}
