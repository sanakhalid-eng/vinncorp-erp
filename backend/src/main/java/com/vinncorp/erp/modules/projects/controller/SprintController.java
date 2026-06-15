package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.SprintRequest;
import com.vinncorp.erp.modules.projects.dto.response.*;
import com.vinncorp.erp.modules.projects.engine.TaskStateResolver;
import com.vinncorp.erp.modules.projects.entity.SprintBurndown;
import com.vinncorp.erp.modules.projects.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprints")
public class SprintController {

    private final SprintService sprintService;
    private final TaskStateResolver taskStateResolver;
    private final BurndownService burndownService;
    private final VelocityService velocityService;
    private final CapacityService capacityService;
    private final SprintAnalyticsService sprintAnalyticsService;
    private final SprintHealthService sprintHealthService;
    private final SprintForecastService sprintForecastService;
    private final SprintCarryForwardService sprintCarryForwardService;
    private final WorkloadBalancingService workloadBalancingService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(summary = "Create sprint", description = "Create a new sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> createSprint(
            @RequestBody SprintRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint created successfully",
                sprintService.createSprint(request, userDetails.getUsername())));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{sprintId}/start")
    @Operation(summary = "Start sprint", description = "Start a sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> startSprint(
            @PathVariable Long sprintId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint started successfully",
                sprintService.startSprint(sprintId, userDetails.getUsername())));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{sprintId}/complete")
    @Operation(summary = "Complete sprint", description = "Complete a sprint and optionally carry forward incomplete tasks")
    public ResponseEntity<ApiResponse<SprintResponse>> completeSprint(
            @PathVariable Long sprintId,
            @RequestParam(defaultValue = "false") boolean carryForward,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint completed successfully",
                sprintService.completeSprint(sprintId, userDetails.getUsername(), carryForward)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project sprints", description = "Retrieve all sprints for a project")
    public ResponseEntity<ApiResponse<List<SprintResponse>>> getProjectSprints(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprints fetched successfully",
                sprintService.getProjectSprints(projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project/{projectId}/active")
    @Operation(summary = "Get active sprint", description = "Retrieve the currently active sprint for a project")
    public ResponseEntity<ApiResponse<SprintResponse>> getActiveSprint(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Active sprint fetched successfully",
                sprintService.getActiveSprint(projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project/{projectId}/backlog")
    @Operation(summary = "Get backlog tasks", description = "Retrieve backlog tasks for a project")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getBacklogTasks(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Backlog tasks fetched successfully",
                sprintService.getBacklogTasks(projectId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}")
    @Operation(summary = "Get sprint by ID", description = "Retrieve a single sprint by its ID")
    public ResponseEntity<ApiResponse<SprintResponse>> getSprintById(
            @PathVariable Long sprintId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint fetched successfully",
                sprintService.getSprintById(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{sprintId}")
    @Operation(summary = "Delete sprint", description = "Delete a sprint by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteSprint(
            @PathVariable Long sprintId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        sprintService.deleteSprint(sprintId, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint deleted successfully", null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/summary")
    @Operation(summary = "Get sprint summary", description = "Retrieve a summary of tasks in a sprint")
    public ResponseEntity<ApiResponse<SprintTaskSummaryResponse>> getSprintSummary(
            @PathVariable Long sprintId
    ) {
        SprintResponse sprintResp = sprintService.getSprintById(sprintId);
        List<TaskStateResponse> taskStates = taskStateResolver.resolveAllByProject(
                sprintResp.getProjectId()
        ).stream()
                .filter(state -> state.getSprintId() != null && state.getSprintId().equals(sprintId))
                .collect(Collectors.toList());

        int total = taskStates.size();
        int completed = (int) taskStates.stream().filter(s -> "DONE".equals(s.getStatus())).count();
        int blocked = (int) taskStates.stream().filter(s -> Boolean.TRUE.equals(s.getBlocked())).count();

        SprintTaskSummaryResponse summary = new SprintTaskSummaryResponse();
        summary.setSprintId(sprintId);
        summary.setSprintName(sprintService.getSprintById(sprintId).getName());
        summary.setTotalTasks(total);
        summary.setCompletedTasks(completed);
        summary.setBlockedTasks(blocked);
        summary.setRemainingTasks(total - completed);
        summary.setTaskStates(taskStates);

        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint summary fetched successfully", summary));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/burndown")
    @Operation(summary = "Get burndown data", description = "Retrieve burndown chart data for a sprint")
    public ResponseEntity<ApiResponse<List<BurndownDataResponse>>> getBurndownData(
            @PathVariable Long sprintId
    ) {
        List<SprintBurndown> burndownList = burndownService.getBurndownData(sprintId);
        SprintResponse sprint = sprintService.getSprintById(sprintId);

        List<BurndownDataResponse> response = new ArrayList<>();
        LocalDate startDate = sprint.getStartDate();
        LocalDate endDate = sprint.getEndDate();

        if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            int totalTasks = burndownList.isEmpty() ? 0 : burndownList.get(0).getTotalTasks();

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                final LocalDate currentDateFinal = currentDate;
                BurndownDataResponse data = new BurndownDataResponse();
                data.setDate(currentDateFinal);

                SprintBurndown snapshot = burndownList.stream()
                        .filter(b -> b.getDate().equals(currentDateFinal))
                        .findFirst()
                        .orElse(null);

                if (snapshot != null) {
                    data.setTotalTasks(snapshot.getTotalTasks());
                    data.setCompletedTasks(snapshot.getCompletedTasks());
                    data.setRemainingTasks(snapshot.getRemainingTasks());
                    data.setBlockedTasks(snapshot.getBlockedTasks());
                } else {
                    data.setTotalTasks(totalTasks);
                    data.setCompletedTasks(0);
                    data.setRemainingTasks(totalTasks);
                    data.setBlockedTasks(0);
                }

                long dayNumber = java.time.temporal.ChronoUnit.DAYS.between(startDate, currentDateFinal) + 1;
                double idealRemaining = totalTasks - (totalTasks * (dayNumber - 1) / (double) totalDays);
                data.setIdealRemaining(Math.max(0, Math.round(idealRemaining * 10.0) / 10.0));

                response.add(data);
                currentDate = currentDate.plusDays(1);
            }
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Burndown data fetched successfully", response));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/velocity")
    @Operation(summary = "Get sprint velocity", description = "Get velocity snapshot for a sprint")
    public ResponseEntity<ApiResponse<VelocityResponse>> getSprintVelocity(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Velocity fetched successfully",
                velocityService.getSprintVelocity(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/health")
    @Operation(summary = "Get sprint health", description = "Assess sprint health with scoring")
    public ResponseEntity<ApiResponse<SprintHealthResponse>> getSprintHealth(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint health assessed successfully",
                sprintHealthService.assessHealth(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/forecast")
    @Operation(summary = "Get sprint forecast", description = "Forecast sprint completion")
    public ResponseEntity<ApiResponse<SprintForecastResponse>> getSprintForecast(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sprint forecast fetched successfully",
                sprintForecastService.forecast(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/workload-balance")
    @Operation(summary = "Get workload balance", description = "Analyze workload distribution across sprint members")
    public ResponseEntity<ApiResponse<WorkloadBalanceResponse>> getWorkloadBalance(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Workload balance analyzed successfully",
                workloadBalancingService.analyze(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/carry-forward")
    @Operation(summary = "Get carry-forward candidates", description = "Get tasks that may need to carry forward")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getCarryForwardCandidates(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Carry-forward candidates fetched successfully",
                sprintCarryForwardService.getCarryForwardCandidates(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/carry-forward/dependency-aware")
    @Operation(summary = "Get dependency-aware rollover", description = "Get tasks + their dependencies for rollover")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getDependencyAwareRollover(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Dependency-aware rollover fetched successfully",
                sprintCarryForwardService.getDependencyAwareRollover(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{sprintId}/capacity")
    @Operation(summary = "Set member capacity", description = "Set capacity for a sprint member")
    public ResponseEntity<ApiResponse<CapacityResponse>> setSprintCapacity(
            @PathVariable Long sprintId,
            @RequestParam Long userId,
            @RequestParam double availableHours,
            @RequestParam(defaultValue = "0") int ptoDays,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Capacity set successfully",
                capacityService.setCapacity(sprintId, userId, availableHours, ptoDays, userDetails.getUsername())));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/capacity")
    @Operation(summary = "Get sprint capacities", description = "Get all member capacities for a sprint")
    public ResponseEntity<ApiResponse<List<CapacityResponse>>> getSprintCapacities(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Capacities fetched successfully",
                capacityService.getSprintCapacities(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/capacity/summary")
    @Operation(summary = "Get capacity summary", description = "Get aggregate capacity summary for a sprint")
    public ResponseEntity<ApiResponse<CapacitySummaryResponse>> getCapacitySummary(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Capacity summary fetched successfully",
                capacityService.getCapacitySummary(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/analytics/burndown")
    @Operation(summary = "Get enhanced burndown", description = "Get burndown with ideal line overlay")
    public ResponseEntity<ApiResponse<List<BurndownDataPoint>>> getEnhancedBurndown(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Burndown data fetched successfully",
                sprintAnalyticsService.getBurndown(sprintId)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{sprintId}/analytics/burnup")
    @Operation(summary = "Get burnup data", description = "Get burnup chart data")
    public ResponseEntity<ApiResponse<List<BurnDataPoint>>> getBurnup(@PathVariable Long sprintId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Burnup data fetched successfully",
                sprintAnalyticsService.getBurnup(sprintId)));
    }
}



