package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Sprint capacity response")
public class CapacityResponse {
    private Long id;
    private Long sprintId;
    private Long userId;
    private String userName;
    private String userEmail;
    private double availableHours;
    private double allocatedHours;
    private double utilizationPercent;
    private int ptoDays;
    private boolean overCapacity;
}



