package com.vinncorp.erp.modules.finance.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExpenseApprovedEvent extends ApplicationEvent {

    private final Long expenseId;
    private final Long workspaceId;
    private final String actorEmail;

    public ExpenseApprovedEvent(Object source, Long expenseId, Long workspaceId, String actorEmail) {
        super(source);
        this.expenseId = expenseId;
        this.workspaceId = workspaceId;
        this.actorEmail = actorEmail;
    }
}
