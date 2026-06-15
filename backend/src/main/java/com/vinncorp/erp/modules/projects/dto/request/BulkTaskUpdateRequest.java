package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkTaskUpdateRequest {
    @NotEmpty
    private List<Long> taskIds;
    private Long statusId;
    private Long assigneeId;
    private String priority;
    private Long sprintId;
}



