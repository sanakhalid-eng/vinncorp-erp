package com.vinncorp.erp.modules.projects.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class TaskFilterRequest {
    private Long sprintId;
    private List<Long> labelIds;
    private Long statusId;
    private Boolean blocked;
    private Boolean hasSubtasks;
    private Long assigneeId;
    private String priority;
    private Boolean inSprint;
}



