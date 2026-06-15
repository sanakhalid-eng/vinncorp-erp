package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.SLAStatus;
import com.vinncorp.erp.modules.projects.enums.SLAType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "SLA response")
public class SLAResponse {
    private Long id;
    private Long workspaceId;
    private Long projectId;
    private Long taskId;
    private String taskTitle;
    private SLAType slaType;
    private Integer responseMinutes;
    private Integer completionMinutes;
    private double warningThresholdPct;
    private double elapsedPercent;
    private long elapsedMinutes;
    private long remainingMinutes;
    private SLAStatus status;
    private String breachedAt;
    private String warnedAt;
    private String resolvedAt;
    private String createdAt;
}



