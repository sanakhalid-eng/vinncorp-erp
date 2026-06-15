package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.CalendarResponse;
import com.vinncorp.erp.modules.projects.dto.response.SprintCalendarResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskCalendarResponse;
import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TimeLog;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSprintRepository;
import com.vinncorp.erp.modules.projects.repository.TimeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final TaskSprintRepository taskSprintRepository;
    private final CalendarService calendarService;
    private final AnalyticsService analyticsService;
    private final BurndownService burndownService;
    private final TimeLogRepository timeLogRepository;

    public byte[] exportTasks(Long projectId, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return exportTasksPdf(projectId);
        }
        return exportTasksCsv(projectId);
    }

    private byte[] exportTasksCsv(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);
        StringWriter writer = new StringWriter();

        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("ID", "Title", "Status", "Priority", "Assignee", "Sprint", "Due Date", "Labels", "Blocked"))) {

            for (Task task : tasks) {
                String sprintName = taskSprintRepository.findByTaskId(task.getId())
                        .map(ts -> ts.getSprint().getName())
                        .orElse("");

                String labels = task.getTaskLabels() != null
                        ? task.getTaskLabels().stream()
                                .filter(tl -> tl.getLabel() != null && tl.getLabel().getDeletedAt() == null)
                                .map(tl -> tl.getLabel().getName())
                                .collect(Collectors.joining(";"))
                        : "";

                printer.printRecord(
                        task.getId(),
                        task.getTitle(),
                        task.getStatusEntity() != null ? task.getStatusEntity().getName() : "",
                        task.getPriority() != null ? task.getPriority().name() : "",
                        task.getAssignee() != null ? task.getAssignee().getName() : "",
                        sprintName,
                        task.getDueDate() != null ? task.getDueDate().toString() : "",
                        labels,
                        "N/A" // Blocked info would need TaskStateResolver
                );
            }
        } catch (Exception e) {
            log.error("Failed to generate tasks CSV", e);
            throw new RuntimeException("Failed to generate CSV", e);
        }

        return writer.toString().getBytes();
    }

    private byte[] exportTasksPdf(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectIdWithAssigneeAndStatus(projectId);
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><title>Task Export</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;}");
        html.append("table{border-collapse:collapse;width:100%;}");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}");
        html.append("th{background-color:#f2f2f2;}</style></head><body>");
        html.append("<h1>Task Export Report</h1>");
        html.append("<p>Project ID: ").append(projectId).append("</p>");
        html.append("<table><thead><tr><th>ID</th><th>Title</th><th>Status</th>");
        html.append("<th>Priority</th><th>Assignee</th><th>Due Date</th></tr></thead><tbody>");

        for (Task task : tasks) {
            html.append("<tr>");
            html.append("<td>").append(task.getId()).append("</td>");
            html.append("<td>").append(escapeHtml(task.getTitle())).append("</td>");
            html.append("<td>").append(task.getStatusEntity() != null ? task.getStatusEntity().getName() : "").append("</td>");
            html.append("<td>").append(task.getPriority() != null ? task.getPriority().name() : "").append("</td>");
            html.append("<td>").append(task.getAssignee() != null ? task.getAssignee().getName() : "").append("</td>");
            html.append("<td>").append(task.getDueDate() != null ? task.getDueDate().toString() : "").append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table></body></html>");
        return htmlToPdf(html.toString());
    }

    private byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public byte[] exportSprintReport(Long sprintId, String format) {
        if (!"pdf".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException("Sprint report only supports PDF format");
        }
        return exportSprintReportPdf(sprintId);
    }

    private byte[] exportSprintReportPdf(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><title>Sprint Report</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;}");
        html.append("h1{color:#333;} .summary{background:#f5f5f5;padding:15px;margin:20px 0;}");
        html.append("</style></head><body>");
        html.append("<h1>Sprint Report</h1>");
        html.append("<div class='summary'>");
        html.append("<h2>").append(escapeHtml(sprint.getName())).append("</h2>");
        html.append("<p><strong>Date Range:</strong> ").append(sprint.getStartDate()).append(" to ").append(sprint.getEndDate()).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(sprint.getStatus()).append("</p>");
        html.append("<p><strong>Total Tasks:</strong> ").append(sprint.getSummaryTotalTasks() != null ? sprint.getSummaryTotalTasks() : 0).append("</p>");
        html.append("<p><strong>Completed Tasks:</strong> ").append(sprint.getSummaryCompletedTasks() != null ? sprint.getSummaryCompletedTasks() : 0).append("</p>");
        Double progress = 0.0;
        if (sprint.getSummaryTotalTasks() != null && sprint.getSummaryTotalTasks() > 0) {
            progress = (sprint.getSummaryCompletedTasks() * 100.0) / sprint.getSummaryTotalTasks();
        }
        html.append("<p><strong>Progress:</strong> ").append(String.format("%.1f", progress)).append("%</p>");
        html.append("</div></body></html>");

        return htmlToPdf(html.toString());
    }

    public byte[] exportAnalytics(Long projectId, String format) {
        if (!"pdf".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException("Analytics export only supports PDF format");
        }
        return exportAnalyticsPdf(projectId);
    }

    private byte[] exportAnalyticsPdf(Long projectId) {
        var analytics = analyticsService.getDashboardSummary(projectId);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><title>Analytics Report</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;}");
        html.append("h1{color:#333;border-bottom:2px solid #333;padding-bottom:10px;}");
        html.append(".kpi{background:#f5f5f5;padding:15px;margin:10px 0;display:inline-block;min-width:150px;margin-right:10px;}");
        html.append("</style></head><body>");
        html.append("<h1>Analytics Report</h1>");
        html.append("<p><strong>Project ID:</strong> ").append(projectId).append("</p>");

        html.append("<h2>Velocity</h2>");
        html.append("<div class='kpi'><strong>Average:</strong> ").append(analytics.getVelocity().getAverage()).append("</div>");
        html.append("<div class='kpi'><strong>Trend:</strong> ").append(analytics.getVelocity().getTrend()).append("</div>");

        html.append("<h2>Performance</h2>");
        html.append("<div class='kpi'><strong>Completion Rate:</strong> ").append(analytics.getCompletionRate()).append("%</div>");
        html.append("<div class='kpi'><strong>Blocked Work Ratio:</strong> ").append(analytics.getBlockedWorkRatio()).append("%</div>");

        if (analytics.getForecast() != null) {
            html.append("<h2>Forecast</h2>");
            html.append("<div class='kpi'><strong>Remaining Tasks:</strong> ").append(analytics.getForecast().getRemainingTasks()).append("</div>");
            html.append("<div class='kpi'><strong>Estimated Sprints:</strong> ").append(analytics.getForecast().getEstimatedSprints()).append("</div>");
        }

        html.append("</body></html>");
        return htmlToPdf(html.toString());
    }

    public byte[] exportTimesheet(Long userId, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return exportTimesheetPdf(userId);
        }
        return exportTimesheetCsv(userId);
    }

    private byte[] exportTimesheetCsv(Long userId) {
        List<TimeLog> timeLogs = timeLogRepository.findByUserIdOrderByLogDateDesc(userId);
        StringWriter writer = new StringWriter();

        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("ID", "Task", "Date", "Hours", "Description"))) {

            for (TimeLog log : timeLogs) {
                printer.printRecord(
                        log.getId(),
                        log.getTask() != null ? log.getTask().getTitle() : "",
                        log.getLogDate() != null ? log.getLogDate().toString() : "",
                        log.getHours(),
                        log.getDescription() != null ? log.getDescription() : ""
                );
            }
        } catch (Exception e) {
            log.error("Failed to generate timesheet CSV", e);
            throw new RuntimeException("Failed to generate CSV", e);
        }

        return writer.toString().getBytes();
    }

    private byte[] exportTimesheetPdf(Long userId) {
        List<TimeLog> timeLogs = timeLogRepository.findByUserIdOrderByLogDateDesc(userId);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><title>Timesheet Report</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;}");
        html.append("table{border-collapse:collapse;width:100%;}");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}");
        html.append("th{background-color:#f2f2f2;}</style></head><body>");
        html.append("<h1>Timesheet Report</h1>");
        html.append("<p><strong>User ID:</strong> ").append(userId).append("</p>");
        html.append("<table><thead><tr><th>ID</th><th>Task</th><th>Date</th><th>Hours</th><th>Description</th></tr></thead><tbody>");

        for (TimeLog log : timeLogs) {
            html.append("<tr>");
            html.append("<td>").append(log.getId()).append("</td>");
            html.append("<td>").append(escapeHtml(log.getTask() != null ? log.getTask().getTitle() : "")).append("</td>");
            html.append("<td>").append(log.getLogDate() != null ? log.getLogDate().toString() : "").append("</td>");
            html.append("<td>").append(log.getHours()).append("</td>");
            html.append("<td>").append(escapeHtml(log.getDescription() != null ? log.getDescription() : "")).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table></body></html>");
        return htmlToPdf(html.toString());
    }

    public byte[] exportCalendar(Long projectId, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException("Calendar export only supports CSV format");
        }
        return exportCalendarCsv(projectId);
    }

    private byte[] exportCalendarCsv(Long projectId) {
        CalendarResponse calendarData = calendarService.getCalendarData(projectId);
        StringWriter writer = new StringWriter();

        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Type", "Name", "Start Date", "End Date", "Status", "Details"))) {

            // Export sprints
            if (calendarData.getSprints() != null) {
                for (SprintCalendarResponse sprint : calendarData.getSprints()) {
                    printer.printRecord(
                            "Sprint",
                            sprint.getName(),
                            sprint.getStartDate(),
                            sprint.getEndDate(),
                            sprint.getStatus(),
                            "Progress: " + sprint.getProgressPercentage() + "%"
                    );
                }
            }

            // Export tasks
            if (calendarData.getTasks() != null) {
                for (TaskCalendarResponse task : calendarData.getTasks()) {
                    printer.printRecord(
                            "Task",
                            task.getTitle(),
                            task.getDueDate(),
                            "",
                            task.getStatus(),
                            "Priority: " + task.getPriority() + ", Assignee: " + (task.getAssigneeName() != null ? task.getAssigneeName() : "Unassigned")
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate calendar CSV", e);
            throw new RuntimeException("Failed to generate CSV", e);
        }

        return writer.toString().getBytes();
    }
}



