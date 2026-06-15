package com.vinncorp.erp.modules.projects.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Capacity request")
public class CapacityRequest {
    private Long userId;
    private double availableHours;
    private int ptoDays;
}



