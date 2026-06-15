package com.vinncorp.erp.modules.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriticalPathResponse {
    private List<CriticalTaskResponse> criticalTasks;
    private int longestChainLength;
    private double totalCriticalityScore;
    private LocalDateTime calculatedAt;
}



