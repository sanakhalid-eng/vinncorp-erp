package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CalendarResponse {
    private List<TaskCalendarResponse> tasks;
    private List<SprintCalendarResponse> sprints;
    private Integer overdueTasksCount;
}



