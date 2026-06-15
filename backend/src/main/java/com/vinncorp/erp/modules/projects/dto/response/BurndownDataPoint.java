package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Burndown data point")
public class BurndownDataPoint {
    private LocalDate date;
    private int remainingTasks;
    private int completedTasks;
    private int totalTasks;
    private double idealRemaining;
}



