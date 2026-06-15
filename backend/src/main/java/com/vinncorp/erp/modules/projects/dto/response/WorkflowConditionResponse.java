package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.WorkflowConditionOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Workflow condition response")
public class WorkflowConditionResponse {
    private Long id;
    private String fieldName;
    private WorkflowConditionOperator operator;
    private String comparisonValue;
}



