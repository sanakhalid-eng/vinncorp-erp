package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Burnup data point")
public class BurnDataPoint {
    private LocalDate date;
    private int completedTasks;
    private int totalTasks;
    private int remainingTasks;
    private double idealCompleted;
}



