package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Throughput metrics with history")
public class ThroughputResponse {
    private Long projectId;
    private int totalCompleted;
    private double weeklyAverage;
    private List<ThroughputEntry> history;

    @Data
    @Builder
    @Schema(description = "Throughput data point")
    public static class ThroughputEntry {
        private String period;
        private int count;
    }
}



