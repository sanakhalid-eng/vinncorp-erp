package com.vinncorp.erp.modules.finance.event;

import com.vinncorp.erp.modules.finance.event.ExpenseApprovedEvent;
import com.vinncorp.erp.modules.finance.event.InvoicePaidEvent;
import com.vinncorp.erp.modules.finance.event.InvoiceSentEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FinanceEventListener {

    private static final Logger log = LoggerFactory.getLogger(FinanceEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvoiceSent(InvoiceSentEvent event) {
        log.info("Invoice {} sent in workspace {} by {}", event.getInvoiceId(), event.getWorkspaceId(), event.getActorEmail());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvoicePaid(InvoicePaidEvent event) {
        log.info("Invoice {} paid in workspace {} by {}", event.getInvoiceId(), event.getWorkspaceId(), event.getActorEmail());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleExpenseApproved(ExpenseApprovedEvent event) {
        log.info("Expense {} approved in workspace {} by {}", event.getExpenseId(), event.getWorkspaceId(), event.getActorEmail());
    }
}
