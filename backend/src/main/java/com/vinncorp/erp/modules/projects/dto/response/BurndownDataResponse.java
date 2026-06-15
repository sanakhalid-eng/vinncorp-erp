package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BurndownDataResponse {
    private LocalDate date;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer remainingTasks;
    private Integer blockedTasks;
    private Double idealRemaining;
}



