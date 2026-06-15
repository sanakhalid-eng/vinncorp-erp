package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Team productivity heatmap")
public class TeamHeatmapResponse {
    private Long projectId;
    private List<MemberProductivity> members;

    @Data
    @Builder
    @Schema(description = "Individual member productivity")
    public static class MemberProductivity {
        private Long userId;
        private String userName;
        private int completedTasks;
        private int totalPoints;
        private double averageCycleTime;
        private double utilizationPercent;
    }
}



