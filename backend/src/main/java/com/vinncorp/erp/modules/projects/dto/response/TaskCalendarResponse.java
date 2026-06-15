package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

@Data
public class TaskCalendarResponse {
    private Long id;
    private String title;
    private String dueDate;
    private String priority;
    private String status;
    private String assigneeName;
    private Long sprintId;
    private String sprintName;
    private String statusColor;
}



