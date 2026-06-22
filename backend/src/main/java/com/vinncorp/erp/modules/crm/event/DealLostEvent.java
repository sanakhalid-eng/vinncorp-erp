package com.vinncorp.erp.modules.crm.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class DealLostEvent extends ApplicationEvent {

    private final Long opportunityId;
    private final Long workspaceId;
    private final Long actorId;
    private final String actorEmail;
    private final String opportunityTitle;
    private final BigDecimal value;

    public DealLostEvent(Object source, Long opportunityId, Long workspaceId, Long actorId, String actorEmail,
                         String opportunityTitle, BigDecimal value) {
        super(source);
        this.opportunityId = opportunityId;
        this.workspaceId = workspaceId;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.opportunityTitle = opportunityTitle;
        this.value = value;
    }
}
