package com.vinncorp.erp.modules.projects.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.modules.projects.entity.BackgroundJob;
import com.vinncorp.erp.modules.projects.repository.BackgroundJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackgroundJobService {

    private final BackgroundJobRepository jobRepository;
    private final ObjectMapper objectMapper;

    private final Map<String, Consumer<BackgroundJob>> jobHandlers = new ConcurrentHashMap<>();

    public void registerHandler(String jobType, Consumer<BackgroundJob> handler) {
        jobHandlers.put(jobType, handler);
        log.info("Registered job handler for type: {}", jobType);
    }

    @Transactional
    public BackgroundJob enqueue(String jobType, Object payload, Long workspaceId, int priority) {
        BackgroundJob job = new BackgroundJob();
        job.setJobType(jobType);
        job.setWorkspaceId(workspaceId);
        job.setPriority(priority);
        job.setStatus("PENDING");
        job.setNextRetryAt(LocalDateTime.now());

        try {
            job.setPayload(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize job payload", e);
        }

        BackgroundJob saved = jobRepository.save(job);
        log.info("Enqueued job: type={}, id={}, priority={}", jobType, saved.getId(), priority);
        return saved;
    }

    @Transactional
    public BackgroundJob enqueue(String jobType, Object payload, Long workspaceId) {
        return enqueue(jobType, payload, workspaceId, 0);
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processPendingJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<BackgroundJob> pendingJobs = jobRepository.findByStatusAndNextRetryAtBefore("PENDING", now);

        for (BackgroundJob job : pendingJobs) {
            processJob(job);
        }
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processRetryJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<BackgroundJob> retryJobs = jobRepository.findByStatusAndNextRetryAtBefore("RETRY", now);

        for (BackgroundJob job : retryJobs) {
            processJob(job);
        }
    }

    private void processJob(BackgroundJob job) {
        Consumer<BackgroundJob> handler = jobHandlers.get(job.getJobType());
        if (handler == null) {
            log.error("No handler registered for job type: {}", job.getJobType());
            job.setStatus("FAILED");
            job.setErrorMessage("No handler registered");
            job.setDeadLetter(true);
            job.setDeadLetterReason("No handler registered for job type: " + job.getJobType());
            jobRepository.save(job);
            return;
        }

        job.setStatus("PROCESSING");
        job.setStartedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            handler.accept(job);

            job.setStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.info("Job completed: type={}, id={}", job.getJobType(), job.getId());
        } catch (Exception e) {
            handleJobFailure(job, e);
        }
    }

    private void handleJobFailure(BackgroundJob job, Exception e) {
        job.setRetryCount(job.getRetryCount() + 1);
        job.setErrorMessage(e.getMessage());

        if (job.getRetryCount() >= job.getMaxRetries()) {
            job.setStatus("FAILED");
            job.setDeadLetter(true);
            job.setDeadLetterReason("Max retries exceeded: " + e.getMessage());
            log.error("Job moved to dead-letter queue: type={}, id={}, error={}",
                    job.getJobType(), job.getId(), e.getMessage());
        } else {
            job.setStatus("RETRY");
            long backoffMs = (long) Math.pow(2, job.getRetryCount()) * 60000;
            job.setNextRetryAt(LocalDateTime.now().plusNanos(backoffMs * 1_000_000));
            log.warn("Job scheduled for retry: type={}, id={}, retry={}/{}, nextRetry={}",
                    job.getJobType(), job.getId(), job.getRetryCount(), job.getMaxRetries(), job.getNextRetryAt());
        }

        jobRepository.save(job);
    }

    public List<BackgroundJob> getDeadLetterJobs() {
        return jobRepository.findByDeadLetterTrue();
    }

    @Transactional
    public boolean retryDeadLetterJob(Long jobId) {
        BackgroundJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null || !job.isDeadLetter()) return false;

        job.setDeadLetter(false);
        job.setDeadLetterReason(null);
        job.setRetryCount(0);
        job.setStatus("PENDING");
        job.setNextRetryAt(LocalDateTime.now());
        jobRepository.save(job);
        log.info("Retrying dead-letter job: id={}", jobId);
        return true;
    }

    public Map<String, Object> getJobStats() {
        return Map.of(
                "pending", jobRepository.countByStatus("PENDING"),
                "processing", jobRepository.countByStatus("PROCESSING"),
                "completed", jobRepository.countByStatus("COMPLETED"),
                "failed", jobRepository.countByStatus("FAILED"),
                "deadLetter", jobRepository.findByDeadLetterTrue().size()
        );
    }
}



