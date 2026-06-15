package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

@Data
public class SprintCalendarResponse {
    private Long id;
    private String name;
    private String startDate;
    private String endDate;
    private String status;
    private Double progressPercentage;
}



