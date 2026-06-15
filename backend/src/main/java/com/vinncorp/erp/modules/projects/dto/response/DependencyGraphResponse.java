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
public class DependencyGraphResponse {
    private List<GraphNode> nodes;
    private List<GraphEdge> edges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphNode {
        private Long id;
        private String title;
        private String status;
        private String priority;
        private Long assigneeId;
        private String assigneeName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphEdge {
        private Long id;
        private Long sourceId;
        private Long targetId;
        private String dependencyType;
        private String description;
    }
}



