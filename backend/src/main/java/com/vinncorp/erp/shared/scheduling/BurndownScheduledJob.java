package com.vinncorp.erp.shared.scheduling;

import com.vinncorp.erp.modules.projects.entity.Sprint;
import com.vinncorp.erp.modules.projects.repository.SprintRepository;
import com.vinncorp.erp.modules.projects.service.BurndownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BurndownScheduledJob {

    private final SprintRepository sprintRepository;
    private final BurndownService burndownService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void generateDailyBurndownSnapshots() {
        LocalDate today = LocalDate.now();
        log.info("Starting daily burndown snapshot job for date: {}", today);

        List<Sprint> activeSprints = sprintRepository.findByStatus("ACTIVE");
        int snapshotsCreated = 0;

        for (Sprint sprint : activeSprints) {
            try {
                burndownService.computeAndSaveSnapshot(sprint.getId(), today);
                snapshotsCreated++;
            } catch (Exception e) {
                log.error("Failed to generate burndown snapshot for sprint {}: {}",
                        sprint.getId(), e.getMessage());
            }
        }

        log.info("Daily burndown snapshot job completed: {} snapshots created for {} active sprints",
                snapshotsCreated, activeSprints.size());
    }
}

