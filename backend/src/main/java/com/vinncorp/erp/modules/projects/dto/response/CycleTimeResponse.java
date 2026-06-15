package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Cycle and lead time analytics")
public class CycleTimeResponse {
    private double averageCycleTime;
    private double averageLeadTime;
    private String trend;
    private List<CycleTimeEntry> history;

    @Data
    @Builder
    @Schema(description = "Cycle time data point")
    public static class CycleTimeEntry {
        private String period;
        private double cycleTime;
        private double leadTime;
    }
}



