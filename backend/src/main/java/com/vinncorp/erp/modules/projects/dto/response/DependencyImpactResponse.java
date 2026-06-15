package com.vinncorp.erp.modules.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependencyImpactResponse {
    private Long taskId;
    private String taskTitle;
    private int totalDependencies;
    private int directDependents;
    private int transitiveDependents;
    private double delayImpactMultiplier;
    private String riskLevel;
}



