package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PortfolioRoadmapItemResponse {
    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private LocalDate milestoneDate;
    private String status;
    private int sortOrder;
}



