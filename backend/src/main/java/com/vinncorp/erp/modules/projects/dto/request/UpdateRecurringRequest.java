package com.vinncorp.erp.modules.projects.dto.request;

import com.vinncorp.erp.modules.projects.enums.RecurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Update recurring task schedule request")
public class UpdateRecurringRequest {

    @Schema(example = "DAILY", description = "DAILY, WEEKLY, MONTHLY, or CUSTOM")
    private RecurrenceType recurrenceType;

    @Schema(example = "1", description = "Interval value (every N days/weeks/months)")
    private Integer intervalValue;

    @Schema(example = "[\"MONDAY\",\"WEDNESDAY\",\"FRIDAY\"]", description = "Days of week for WEEKLY type")
    private List<String> daysOfWeek;

    @Schema(example = "15", description = "Day of month for MONTHLY type (1-31)")
    private Integer dayOfMonth;

    @Schema(example = "2026-06-01T08:00:00", description = "New next run date")
    private LocalDateTime nextRunAt;

    @Schema(example = "2026-12-31T23:59:59", description = "Optional end date for recurrence")
    private LocalDateTime endsAt;

    @Schema(example = "100", description = "Optional max number of occurrences")
    private Integer maxOccurrences;
}



