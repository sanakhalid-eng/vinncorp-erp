package com.vinncorp.erp.modules.finance.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InvoicePaidEvent extends ApplicationEvent {

    private final Long invoiceId;
    private final Long workspaceId;
    private final String actorEmail;

    public InvoicePaidEvent(Object source, Long invoiceId, Long workspaceId, String actorEmail) {
        super(source);
        this.invoiceId = invoiceId;
        this.workspaceId = workspaceId;
        this.actorEmail = actorEmail;
    }
}
