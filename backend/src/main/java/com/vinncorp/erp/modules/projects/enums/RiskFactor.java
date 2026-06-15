package com.vinncorp.erp.modules.projects.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Individual risk factor contributing to overall score")
public class RiskFactor {
    private String name;
    private double score;
    private String severity;
    private String description;
}



