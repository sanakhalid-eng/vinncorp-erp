package com.vinncorp.erp.modules.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriticalTaskResponse {
    private Long taskId;
    private String taskTitle;
    private int dependencyDepth;
    private double criticalityScore;
    private boolean isOnCriticalPath;
    private LocalDateTime calculatedAt;
}



