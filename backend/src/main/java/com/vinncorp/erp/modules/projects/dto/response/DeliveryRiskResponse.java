package com.vinncorp.erp.modules.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRiskResponse {
    private Long taskId;
    private String taskTitle;
    private double delayProbability;
    private int estimatedDelayDays;
    private String riskLevel;
    private List<String> blockingFactors;
}



