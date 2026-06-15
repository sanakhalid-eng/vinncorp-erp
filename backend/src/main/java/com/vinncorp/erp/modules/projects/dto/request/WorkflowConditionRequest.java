package com.vinncorp.erp.modules.projects.dto.request;

import com.vinncorp.erp.modules.projects.enums.WorkflowConditionOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Workflow condition request")
public class WorkflowConditionRequest {
    private String fieldName;
    private WorkflowConditionOperator operator;
    private String comparisonValue;
}



