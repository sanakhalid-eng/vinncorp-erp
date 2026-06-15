package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.CalendarResponse;
import com.vinncorp.erp.modules.projects.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "System")
public class CalendarController {

    private final CalendarService calendarService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{projectId}/calendar")
    @Operation(summary = "Get calendar data", description = "Retrieve calendar data for a project")
    public ResponseEntity<ApiResponse<CalendarResponse>> getCalendarData(
            @PathVariable Long projectId
    ) {
        CalendarResponse calendarData = calendarService.getCalendarData(projectId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Calendar data fetched successfully", calendarData));
    }
}



