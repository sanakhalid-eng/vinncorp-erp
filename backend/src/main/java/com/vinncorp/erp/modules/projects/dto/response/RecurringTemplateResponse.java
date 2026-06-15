package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.RecurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Recurring task template response")
public class RecurringTemplateResponse {

    private Long id;
    private Long workspaceId;
    private Long projectId;
    private Long templateTaskId;
    private String templateTaskTitle;
    private RecurrenceType recurrenceType;
    private int intervalValue;
    private List<String> daysOfWeek;
    private Integer dayOfMonth;
    private LocalDateTime nextRunAt;
    private LocalDateTime lastGeneratedAt;
    private boolean active;
    private boolean paused;
    private LocalDateTime endsAt;
    private Integer maxOccurrences;
    private int generatedCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



