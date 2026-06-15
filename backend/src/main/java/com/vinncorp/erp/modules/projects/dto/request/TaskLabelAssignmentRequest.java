package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TaskLabelAssignmentRequest {

    @NotEmpty(message = "At least one label ID is required")
    private List<Long> labelIds;
}



