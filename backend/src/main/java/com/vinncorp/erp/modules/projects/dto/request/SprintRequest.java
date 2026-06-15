package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SprintRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Sprint name is required")
    private String name;

    private String goal;

    private LocalDate startDate;

    private LocalDate endDate;
}



