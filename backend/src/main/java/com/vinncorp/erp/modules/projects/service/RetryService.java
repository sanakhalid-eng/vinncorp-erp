package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.entity.RetryQueue;
import com.vinncorp.erp.modules.projects.repository.RetryQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {
    private final RetryQueueRepository retryQueueRepository;

    @Transactional
    public RetryQueue enqueue(String type, String entityType, Long entityId, String payload, int maxRetries) {
        RetryQueue entry = new RetryQueue();
        entry.setType(type);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setPayload(payload);
        entry.setStatus("PENDING");
        entry.setMaxRetries(maxRetries);
        entry.setRetryCount(0);
        return retryQueueRepository.save(entry);
    }

    @Transactional
    public void markRetried(Long id, boolean success, String errorMessage) {
        retryQueueRepository.findById(id).ifPresent(entry -> {
            if (success) {
                entry.setStatus("COMPLETED");
                entry.setCompletedAt(LocalDateTime.now());
            } else {
                entry.setRetryCount(entry.getRetryCount() + 1);
                entry.setLastError(errorMessage);
                entry.setLastRetryAt(LocalDateTime.now());

                if (entry.getRetryCount() >= entry.getMaxRetries()) {
                    entry.setStatus("DEAD_LETTER");
                    log.warn("Retry entry {} moved to DEAD_LETTER after {} retries", id, entry.getMaxRetries());
                } else {
                    entry.setStatus("RETRYING");
                    entry.setNextRetryAt(calculateNextRetry(entry.getRetryCount()));
                }

                String history = entry.getErrorHistory();
                String newEntry = LocalDateTime.now() + ": " + (errorMessage != null ? errorMessage : "unknown");
                entry.setErrorHistory(history != null ? history + "\n" + newEntry : newEntry);
            }
            retryQueueRepository.save(entry);
        });
    }

    public long getQueueBacklog() {
        return retryQueueRepository.countByStatus("PENDING") + retryQueueRepository.countByStatus("RETRYING");
    }

    public long getDeadLetterCount() {
        return retryQueueRepository.countByStatus("DEAD_LETTER");
    }

    private LocalDateTime calculateNextRetry(int retryCount) {
        int minutes = switch (retryCount) {
            case 1 -> 1;
            case 2 -> 5;
            case 3 -> 15;
            case 4 -> 60;
            case 5 -> 360;
            default -> 1;
        };
        return LocalDateTime.now().plusMinutes(minutes);
    }
}



