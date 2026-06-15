package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ActivityIntelligenceResponse {
    private Long projectId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String summaryType;
    private List<String> highlights;
    private Map<String, Object> metrics;
}



