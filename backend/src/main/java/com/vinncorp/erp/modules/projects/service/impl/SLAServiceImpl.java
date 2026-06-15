package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.dto.request.SLARequest;
import com.vinncorp.erp.modules.projects.dto.response.SLABreachReportResponse;
import com.vinncorp.erp.modules.projects.dto.response.SLAResponse;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskSLA;
import com.vinncorp.erp.modules.projects.enums.SLAStatus;
import com.vinncorp.erp.modules.projects.enums.SLAType;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSLARepository;
import com.vinncorp.erp.modules.projects.service.SLAService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SLAServiceImpl implements SLAService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TaskSLARepository slaRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public SLAResponse configureSLA(SLARequest request, String email) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        TaskSLA sla = slaRepository.findByTaskIdAndSlaType(request.getTaskId(), request.getSlaType())
                .orElse(new TaskSLA());

        sla.setWorkspaceId(request.getWorkspaceId());
        sla.setProjectId(request.getProjectId());
        sla.setTaskId(request.getTaskId());
        sla.setSlaType(request.getSlaType());
        sla.setResponseMinutes(request.getResponseMinutes());
        sla.setCompletionMinutes(request.getCompletionMinutes());
        sla.setWarningThresholdPct(request.getWarningThresholdPct());
        sla.setStatus(SLAStatus.ACTIVE);

        if (sla.getCreatedAt() == null) {
            sla.setCreatedAt(LocalDateTime.now());
        }

        sla = slaRepository.save(sla);
        log.info("SLA configured for task {}: type={}", request.getTaskId(), request.getSlaType());
        return toResponse(sla, task);
    }

    @Override
    public SLAResponse getTaskSLA(Long taskId, SLAType slaType) {
        TaskSLA sla = slaRepository.findByTaskIdAndSlaType(taskId, slaType)
                .orElseThrow(() -> new ResourceNotFoundException("SLA not found for task"));
        Task task = taskRepository.findById(taskId)
                .orElse(null);
        return toResponse(sla, task);
    }

    @Override
    public List<SLAResponse> getProjectSLAs(Long projectId) {
        return slaRepository.findByProjectId(projectId).stream()
                .map(sla -> {
                    Task task = taskRepository.findById(sla.getTaskId()).orElse(null);
                    return toResponse(sla, task);
                })
                .collect(Collectors.toList());
    }

    @Override
    public SLABreachReportResponse getSLAReport(Long projectId) {
        List<TaskSLA> all = slaRepository.findByProjectId(projectId);
        long active = all.stream().filter(s -> s.getStatus() == SLAStatus.ACTIVE).count();
        long breached = all.stream().filter(s -> s.getStatus() == SLAStatus.BREACHED).count();
        long warned = all.stream().filter(s -> s.getStatus() == SLAStatus.WARNED).count();
        long resolved = all.stream().filter(s -> s.getStatus() == SLAStatus.RESOLVED).count();

        return SLABreachReportResponse.builder()
                .projectId(projectId)
                .totalActive(active)
                .totalBreached(breached)
                .totalWarned(warned)
                .totalResolved(resolved)
                .activeSLAs(all.stream().filter(s -> s.getStatus() == SLAStatus.ACTIVE)
                        .map(s -> toResponse(s, taskRepository.findById(s.getTaskId()).orElse(null)))
                        .collect(Collectors.toList()))
                .breachedSLAs(all.stream().filter(s -> s.getStatus() == SLAStatus.BREACHED)
                        .map(s -> toResponse(s, taskRepository.findById(s.getTaskId()).orElse(null)))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public void checkAndUpdateSLA(Long taskId) {
        for (SLAType type : SLAType.values()) {
            slaRepository.findByTaskIdAndSlaType(taskId, type).ifPresent(sla -> {
                if (sla.getStatus() != SLAStatus.ACTIVE) return;
                long elapsed = ChronoUnit.MINUTES.between(sla.getCreatedAt(), LocalDateTime.now());
                Integer slaMinutes = type == SLAType.RESPONSE ? sla.getResponseMinutes() : sla.getCompletionMinutes();
                if (slaMinutes == null || slaMinutes <= 0) return;

                double elapsedPct = (elapsed * 100.0) / slaMinutes;

                if (elapsedPct >= 100) {
                    sla.setStatus(SLAStatus.BREACHED);
                    sla.setBreachedAt(LocalDateTime.now());
                    log.warn("SLA BREACHED for task {}: type={}, elapsed={}min", taskId, type, elapsed);
                } else if (elapsedPct >= sla.getWarningThresholdPct() && sla.getWarnedAt() == null) {
                    sla.setWarnedAt(LocalDateTime.now());
                    sla.setStatus(SLAStatus.WARNED);
                    log.info("SLA warning for task {}: type={}, {:.1f}% elapsed", taskId, type, elapsedPct);
                }
                slaRepository.save(sla);
            });
        }
    }

    @Override
    @Transactional
    public void startSLATimer(Task task, String email) {
    }

    @Override
    @Transactional
    public void resolveSLA(Long taskId, SLAType slaType) {
        slaRepository.findByTaskIdAndSlaType(taskId, slaType).ifPresent(sla -> {
            sla.setStatus(SLAStatus.RESOLVED);
            sla.setResolvedAt(LocalDateTime.now());
            slaRepository.save(sla);
            log.info("SLA resolved for task {}: type={}", taskId, slaType);
        });
    }

    @Override
    public long countActiveByWorkspace(Long workspaceId) {
        return slaRepository.countByWorkspaceIdAndStatus(workspaceId, SLAStatus.ACTIVE);
    }

    @Override
    public long countBreachedByProject(Long projectId) {
        return slaRepository.countByProjectIdAndStatus(projectId, SLAStatus.BREACHED);
    }

    private SLAResponse toResponse(TaskSLA sla, Task task) {
        long elapsedMinutes = ChronoUnit.MINUTES.between(sla.getCreatedAt(), LocalDateTime.now());
        Integer slaMinutes = sla.getSlaType() == SLAType.RESPONSE
                ? sla.getResponseMinutes() : sla.getCompletionMinutes();
        double elapsedPct = slaMinutes != null && slaMinutes > 0
                ? (elapsedMinutes * 100.0) / slaMinutes : 0;
        long remaining = slaMinutes != null ? Math.max(0, slaMinutes - elapsedMinutes) : 0;

        return SLAResponse.builder()
                .id(sla.getId())
                .workspaceId(sla.getWorkspaceId())
                .projectId(sla.getProjectId())
                .taskId(sla.getTaskId())
                .taskTitle(task != null ? task.getTitle() : null)
                .slaType(sla.getSlaType())
                .responseMinutes(sla.getResponseMinutes())
                .completionMinutes(sla.getCompletionMinutes())
                .warningThresholdPct(sla.getWarningThresholdPct())
                .elapsedPercent(elapsedPct)
                .elapsedMinutes(elapsedMinutes)
                .remainingMinutes(remaining)
                .status(sla.getStatus())
                .breachedAt(sla.getBreachedAt() != null ? sla.getBreachedAt().format(DTF) : null)
                .warnedAt(sla.getWarnedAt() != null ? sla.getWarnedAt().format(DTF) : null)
                .resolvedAt(sla.getResolvedAt() != null ? sla.getResolvedAt().format(DTF) : null)
                .createdAt(sla.getCreatedAt() != null ? sla.getCreatedAt().format(DTF) : null)
                .build();
    }
}



