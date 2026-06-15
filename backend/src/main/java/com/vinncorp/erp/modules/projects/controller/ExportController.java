package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Reports & Analytics")
public class ExportController {

    private final ExportService exportService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/export/tasks")
    @Operation(summary = "Export project tasks", description = "Export tasks for a project in CSV or PDF format")
    public org.springframework.http.ResponseEntity<byte[]> exportTasks(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "csv") String format) {
        byte[] data = exportService.exportTasks(projectId, format);
        String fileName = "project-" + projectId + "-tasks." + format;
        return buildResponse(data, fileName, getMediaType(format));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sprints/{sprintId}/export")
    @Operation(summary = "Export sprint report", description = "Export a sprint report in PDF format")
    public org.springframework.http.ResponseEntity<byte[]> exportSprintReport(
            @PathVariable Long sprintId,
            @RequestParam(defaultValue = "pdf") String format) {
        byte[] data = exportService.exportSprintReport(sprintId, format);
        String fileName = "sprint-" + sprintId + "-report.pdf";
        return buildResponse(data, fileName, MediaType.APPLICATION_PDF);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/export/analytics")
    @Operation(summary = "Export project analytics", description = "Export analytics for a project in PDF format")
    public org.springframework.http.ResponseEntity<byte[]> exportAnalytics(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "pdf") String format) {
        byte[] data = exportService.exportAnalytics(projectId, format);
        String fileName = "project-" + projectId + "-analytics.pdf";
        return buildResponse(data, fileName, MediaType.APPLICATION_PDF);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/projects/{projectId}/export/calendar")
    @Operation(summary = "Export project calendar", description = "Export calendar data for a project in CSV format")
    public org.springframework.http.ResponseEntity<byte[]> exportCalendar(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "csv") String format) {
        byte[] data = exportService.exportCalendar(projectId, format);
        String fileName = "project-" + projectId + "-calendar.csv";
        return buildResponse(data, fileName, MediaType.parseMediaType("text/csv"));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/{userId}/export/timesheet")
    @Operation(summary = "Export user timesheet", description = "Export timesheet data for a user in CSV or PDF format")
    public org.springframework.http.ResponseEntity<byte[]> exportTimesheet(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "csv") String format) {
        byte[] data = exportService.exportTimesheet(userId, format);
        String fileName = "user-" + userId + "-timesheet." + format;
        return buildResponse(data, fileName, getMediaType(format));
    }

    private org.springframework.http.ResponseEntity<byte[]> buildResponse(byte[] data, String fileName, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename(fileName).build());
        headers.setContentLength(data.length);
        return new org.springframework.http.ResponseEntity<>(data, headers, org.springframework.http.HttpStatus.OK);
    }

    private MediaType getMediaType(String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return MediaType.APPLICATION_PDF;
        }
        return MediaType.parseMediaType("text/csv");
    }
}



