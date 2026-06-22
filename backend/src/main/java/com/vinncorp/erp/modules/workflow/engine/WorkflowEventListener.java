package com.vinncorp.erp.modules.workflow.engine;
import com.vinncorp.erp.modules.projects.event.DomainEvent;

public interface WorkflowEventListener {
void onDomainEvent(DomainEvent event);
} 