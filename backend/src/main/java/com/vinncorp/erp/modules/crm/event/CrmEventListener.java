package com.vinncorp.erp.modules.crm.event;

import com.vinncorp.erp.platform.notification.enums.NotificationType;
import com.vinncorp.erp.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrmEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLeadConverted(LeadConvertedEvent event) {
        try {
            String message = String.format("Lead converted to customer (Customer #%d, Contact #%d)",
                    event.getCustomerId(), event.getContactId());
            notificationService.createNotification(
                    event.getActorId(),
                    event.getActorId(),
                    NotificationType.TASK_ASSIGNED,
                    message,
                    event.getLeadId(),
                    "LEAD",
                    null,
                    null,
                    "/crm/leads/" + event.getLeadId(),
                    "LEAD_CONVERTED:" + event.getLeadId(),
                    "CRM:" + event.getWorkspaceId(),
                    "MEDIUM"
            );
            log.info("CRM notification sent for lead conversion: leadId={}", event.getLeadId());
        } catch (Exception e) {
            log.error("Failed to send lead conversion notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDealWon(DealWonEvent event) {
        try {
            String message = String.format("Deal won: %s ($%s)%s",
                    event.getOpportunityTitle(),
                    event.getValue() != null ? event.getValue().toPlainString() : "0",
                    event.getProjectId() != null ? " — Project created" : "");
            notificationService.createNotification(
                    event.getActorId(),
                    event.getActorId(),
                    NotificationType.TASK_ASSIGNED,
                    message,
                    event.getOpportunityId(),
                    "OPPORTUNITY",
                    event.getProjectId(),
                    null,
                    event.getProjectId() != null ? "/projects/" + event.getProjectId() : "/crm/opportunities/" + event.getOpportunityId(),
                    "DEAL_WON:" + event.getOpportunityId(),
                    "CRM:" + event.getWorkspaceId(),
                    "HIGH"
            );
            log.info("CRM notification sent for deal won: oppId={}", event.getOpportunityId());
        } catch (Exception e) {
            log.error("Failed to send deal won notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDealLost(DealLostEvent event) {
        try {
            String message = String.format("Deal lost: %s ($%s)",
                    event.getOpportunityTitle(),
                    event.getValue() != null ? event.getValue().toPlainString() : "0");
            notificationService.createNotification(
                    event.getActorId(),
                    event.getActorId(),
                    NotificationType.STATUS_CHANGED,
                    message,
                    event.getOpportunityId(),
                    "OPPORTUNITY",
                    null,
                    null,
                    "/crm/opportunities/" + event.getOpportunityId(),
                    "DEAL_LOST:" + event.getOpportunityId(),
                    "CRM:" + event.getWorkspaceId(),
                    "MEDIUM"
            );
            log.info("CRM notification sent for deal lost: oppId={}", event.getOpportunityId());
        } catch (Exception e) {
            log.error("Failed to send deal lost notification: {}", e.getMessage());
        }
    }
}
