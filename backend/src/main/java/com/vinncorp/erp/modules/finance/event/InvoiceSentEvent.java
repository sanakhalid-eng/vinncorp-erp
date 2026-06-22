package com.vinncorp.erp.modules.finance.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.Clock;

@Getter
public class InvoiceSentEvent extends ApplicationEvent {

    private final Long invoiceId;
    private final Long workspaceId;
    private final String actorEmail;

    public InvoiceSentEvent(Object source, Long invoiceId, Long workspaceId, String actorEmail) {
        super(source);
        this.invoiceId = invoiceId;
        this.workspaceId = workspaceId;
        this.actorEmail = actorEmail;
    }
}
