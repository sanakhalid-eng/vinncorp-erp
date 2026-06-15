package com.vinncorp.erp.modules.projects.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskProgressResponse {
    private Long parentTaskId;
    private int total;
    private int completed;
    private int pending;
    private double completionPercentage;
    private List<TaskResponse> subtasks;
}



