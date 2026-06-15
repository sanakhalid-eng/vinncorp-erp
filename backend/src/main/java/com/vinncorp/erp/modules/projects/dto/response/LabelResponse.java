package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LabelResponse {
    private Long id;
    private String name;
    private String color;
    private Long projectId;
    private String projectName;
    private int usageCount;
    private LocalDateTime createdAt;
}



