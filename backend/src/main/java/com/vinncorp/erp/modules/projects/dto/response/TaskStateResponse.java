package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class TaskStateResponse {
    private Long taskId;
    private Boolean blocked;
    private Boolean crossSprintDependency;
    private Boolean inSprint;
    private Long sprintId;
    private String sprintStatus;
    private Integer subtaskCompletionPercentage;
    private Boolean hasSubtasks;
    private List<LabelResponse> labels;
    private String status;
    private String statusName;
    private Long statusId;
    private String priority;
    private Long assigneeId;
    private String assigneeName;
    private Long projectId;
}



