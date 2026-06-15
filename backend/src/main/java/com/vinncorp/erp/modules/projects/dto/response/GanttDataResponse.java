package com.vinncorp.erp.modules.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GanttDataResponse {
    private List<GanttTask> tasks;
    private List<GanttSprint> sprints;
    private List<GanttDependency> dependencies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GanttTask {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime dueDate;
        private String priority;
        private String status;
        private String statusColor;
        private Long assigneeId;
        private String assigneeName;
        private Long projectId;
        private Long parentTaskId;
        private Integer storyPoints;
        private double progress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GanttSprint {
        private Long id;
        private String name;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String status;
        private Double progressPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GanttDependency {
        private Long id;
        private Long sourceTaskId;
        private Long targetTaskId;
        private String dependencyType;
    }
}



