package com.vinncorp.erp.modules.projects.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request to plan a sprint composition")
public class SprintPlanRequest {
    private Long sprintId;
    private List<Long> taskIds;
    private Integer capacityOverride;
}



