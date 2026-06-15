package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Capacity summary response")
public class CapacitySummaryResponse {
    private Long sprintId;
    private String sprintName;
    private int totalMembers;
    private double totalAvailableHours;
    private double totalAllocatedHours;
    private double averageUtilization;
    private int overCapacityCount;
    private int underUtilizedCount;
    private List<CapacityResponse> members;
}



