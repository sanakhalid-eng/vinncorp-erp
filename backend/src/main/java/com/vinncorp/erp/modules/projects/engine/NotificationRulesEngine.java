package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.entity.NotificationPreference;
import com.vinncorp.erp.modules.projects.event.DomainEvent;
import com.vinncorp.erp.modules.projects.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRulesEngine {

    private final NotificationPreferenceRepository preferenceRepository;

    public boolean shouldNotify(Long userId, DomainEvent.Type eventType) {
        Optional<NotificationPreference> prefs = preferenceRepository.findByUserId(userId);
        if (prefs.isEmpty()) {
            return true; // default: notify all
        }

        NotificationPreference p = prefs.get();

        return switch (eventType) {
            case TASK_ASSIGNED -> p.isTaskAssigned();
            case TASK_UNASSIGNED -> p.isTaskUnassigned();
            case TASK_STATUS_CHANGED -> p.isTaskStatusChanged();
            case TASK_CREATED -> p.isTaskCreated();
            case COMMENT_MENTIONED -> p.isCommentMentioned();
            case FILE_UPLOADED -> p.isFileUploaded();
            case DUE_DATE_APPROACHING, DUE_DATE_OVERDUE -> p.isDueDateReminder();
            default -> true;
        };
    }

    public boolean shouldSkipNotification(Long actorId, Long targetUserId) {
        return actorId != null && actorId.equals(targetUserId);
    }
}



