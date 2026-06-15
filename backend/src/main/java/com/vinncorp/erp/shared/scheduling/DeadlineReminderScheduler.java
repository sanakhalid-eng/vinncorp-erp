package com.vinncorp.erp.shared.scheduling;

import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.event.TaskDeadlineReminderEvent;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DeadlineReminderScheduler {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Run every hour to check for upcoming deadlines (tasks due in next 24 hours)
    @Scheduled(cron = "0 0 * * * *")
    public void checkDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);

        // Find tasks due in next 24 hours that haven't had reminder sent
        List<Task> tasksDueSoon = taskRepository.findTasksDueSoonAndReminderNotSent(now, next24Hours);

        for (Task task : tasksDueSoon) {
            if (task.getAssignee() != null) {
                eventPublisher.publishEvent(new TaskDeadlineReminderEvent(this, task, task.getAssignee()));
                task.setReminderSent(true);
                taskRepository.save(task);
            }
        }
    }
}
