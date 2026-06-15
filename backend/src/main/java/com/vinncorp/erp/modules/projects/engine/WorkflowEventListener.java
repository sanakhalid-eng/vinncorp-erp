package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.event.DomainEvent;

public interface WorkflowEventListener {

    void onDomainEvent(DomainEvent event);
}



