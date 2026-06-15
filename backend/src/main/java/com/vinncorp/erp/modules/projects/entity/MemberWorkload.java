package com.vinncorp.erp.modules.projects.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Member workload detail")
public class MemberWorkload {
    private Long userId;
    private String userName;
    private int taskCount;
    private double estimatedHours;
    private double assignedPoints;
    private double blockedWorkPercent;
    private boolean overloaded;
    private boolean underutilized;
}



