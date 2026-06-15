package com.vinncorp.erp.modules.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockedStatusResponse {
    private boolean blocked;
    private List<BlockingTaskInfo> blockingTasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockingTaskInfo {
        private Long taskId;
        private String taskTitle;
        private String status;
        private Long dependencyId;
        private String dependencyType;
        private String description;
    }
}



