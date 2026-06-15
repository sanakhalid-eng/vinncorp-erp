package com.vinncorp.erp.modules.projects.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectFromTemplateRequest {
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private String priority;
    private String tags;
    private String category;
    private String objectives;
    private Double budget;
    private String currency;
    private Long projectManagerId;
    private Boolean isActive;
    private Boolean isPublic;
}



