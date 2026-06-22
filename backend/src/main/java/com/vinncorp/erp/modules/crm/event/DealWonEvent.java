package com.vinncorp.erp.modules.crm.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class DealWonEvent extends ApplicationEvent {

    private final Long opportunityId;
    private final Long workspaceId;
    private final Long actorId;
    private final String actorEmail;
    private final String opportunityTitle;
    private final BigDecimal value;
    private final Long projectId;

    public DealWonEvent(Object source, Long opportunityId, Long workspaceId, Long actorId, String actorEmail,
                        String opportunityTitle, BigDecimal value, Long projectId) {
        super(source);
        this.opportunityId = opportunityId;
        this.workspaceId = workspaceId;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.opportunityTitle = opportunityTitle;
        this.value = value;
        this.projectId = projectId;
    }
}
