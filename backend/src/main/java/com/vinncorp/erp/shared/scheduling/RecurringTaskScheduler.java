package com.vinncorp.erp.shared.scheduling;

import com.vinncorp.erp.modules.projects.service.RecurringTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTaskScheduler {

    private final RecurringTaskService recurringTaskService;

    @Scheduled(cron = "0 0 * * * *")
    public void generateRecurringTasks() {
        log.info("Starting recurring task generation scan");
        try {
            recurringTaskService.generateNextOccurrences();
            log.info("Recurring task generation scan completed");
        } catch (Exception e) {
            log.error("Recurring task generation scan failed: {}", e.getMessage(), e);
        }
    }
}


