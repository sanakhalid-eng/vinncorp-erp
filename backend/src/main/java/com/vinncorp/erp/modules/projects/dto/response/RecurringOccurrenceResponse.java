package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "Recurring task occurrence response")
public class RecurringOccurrenceResponse {

    private Long id;
    private Long recurringTemplateId;
    private Long generatedTaskId;
    private String generatedTaskTitle;
    private LocalDate occurrenceDate;
    private String generationStatus;
    private LocalDateTime createdAt;
}



