package com.vinncorp.erp.modules.projects.dto.request;

import com.vinncorp.erp.modules.projects.enums.SLAType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "SLA configuration request")
public class SLARequest {
    private Long workspaceId;
    private Long projectId;
    private Long taskId;
    private SLAType slaType;
    private Integer responseMinutes;
    private Integer completionMinutes;
    private double warningThresholdPct = 80.0;
}



