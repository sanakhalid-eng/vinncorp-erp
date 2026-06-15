package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Capacity risk assessment for a sprint")
public class CapacityRiskResponse {
    private Long sprintId;
    private String sprintName;
    private int totalCapacity;
    private int allocatedPoints;
    private double allocationPercent;
    private List<String> risks;
    private boolean isOverloaded;
}



