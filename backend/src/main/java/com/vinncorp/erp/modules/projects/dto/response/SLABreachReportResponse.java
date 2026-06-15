package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "SLA breach report response")
public class SLABreachReportResponse {
    private Long projectId;
    private String projectName;
    private long totalActive;
    private long totalBreached;
    private long totalWarned;
    private long totalResolved;
    private List<SLAResponse> activeSLAs;
    private List<SLAResponse> breachedSLAs;
}



