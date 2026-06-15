package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Smart assignment response")
public class SmartAssignmentResponse {
    private Long taskId;
    private String taskTitle;
    private Long assignedUserId;
    private String assignedUserName;
    private String reason;
    private List<String> factors;
    private double confidenceScore;
}



