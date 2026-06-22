package com.vinncorp.erp.modules.crm.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadConvertedEvent extends ApplicationEvent {

    private final Long leadId;
    private final Long workspaceId;
    private final Long actorId;
    private final String actorEmail;
    private final Long customerId;
    private final Long contactId;
    private final Long opportunityId;

    public LeadConvertedEvent(Object source, Long leadId, Long workspaceId, Long actorId, String actorEmail,
                              Long customerId, Long contactId, Long opportunityId) {
        super(source);
        this.leadId = leadId;
        this.workspaceId = workspaceId;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.customerId = customerId;
        this.contactId = contactId;
        this.opportunityId = opportunityId;
    }
}
