package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SprintResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private int totalTasks;
    private int completedTasks;
    private double progressPercentage;
    private Integer summaryTotalTasks;
    private Integer summaryCompletedTasks;
    private Integer summaryCarriedForward;
    private LocalDate completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


