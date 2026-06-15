package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.entity.ScheduledJob;
import com.vinncorp.erp.modules.projects.entity.ScheduledJobExecution;
import com.vinncorp.erp.modules.projects.repository.ScheduledJobExecutionRepository;
import com.vinncorp.erp.modules.projects.repository.ScheduledJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobTrackerService {
    private final ScheduledJobRepository jobRepository;
    private final ScheduledJobExecutionRepository executionRepository;

    public Long startExecution(String jobName) {
        ScheduledJob job = jobRepository.findByJobName(jobName)
                .orElseGet(() -> {
                    ScheduledJob newJob = new ScheduledJob();
                    newJob.setJobName(jobName);
                    return jobRepository.save(newJob);
                });

        ScheduledJobExecution execution = new ScheduledJobExecution();
        execution.setJob(job);
        execution.setStartedAt(LocalDateTime.now());
        execution.setStatus("RUNNING");
        execution.setTriggeredBy("SCHEDULER");
        execution = executionRepository.save(execution);
        return execution.getId();
    }

    public void completeExecution(Long executionId, boolean success, String errorMessage) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            ScheduledJob job = execution.getJob();
            LocalDateTime now = LocalDateTime.now();
            long duration = Duration.between(execution.getStartedAt(), now).toMillis();

            execution.setCompletedAt(now);
            execution.setDurationMs(duration);
            execution.setStatus(success ? "SUCCESS" : "FAILED");
            execution.setErrorMessage(errorMessage);
            executionRepository.save(execution);

            job.setLastRunAt(now);
            job.setLastDurationMs(duration);
            job.setLastStatus(success ? "SUCCESS" : "FAILED");
            if (!success) job.setLastError(errorMessage);
            job.setTotalRuns(job.getTotalRuns() + 1);
            if (success) job.setSuccessRuns(job.getSuccessRuns() + 1);
            else job.setFailureRuns(job.getFailureRuns() + 1);
            jobRepository.save(job);
        });
    }

    public List<ScheduledJobExecution> getRecentExecutions(int limit) {
        return executionRepository.findTop10ByOrderByStartedAtDesc();
    }

    public List<ScheduledJobExecution> getFailedExecutions(int limit) {
        return executionRepository.findTop10ByStatusOrderByStartedAtDesc("FAILED");
    }

    public List<ScheduledJob> getAllJobs() {
        return jobRepository.findAll();
    }

    public long getFailedJobCount() {
        return jobRepository.findAll().stream()
                .filter(j -> "FAILED".equals(j.getLastStatus()))
                .count();
    }
}



