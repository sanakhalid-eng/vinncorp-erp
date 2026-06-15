package com.vinncorp.erp.modules.projects.dto.request;

import com.vinncorp.erp.modules.projects.enums.DependencyType;
import lombok.Data;

@Data
public class CreateDependencyRequest {
    private Long dependsOnTaskId;
    private DependencyType dependencyType;
    private String description;
}



