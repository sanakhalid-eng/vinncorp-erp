package com.vinncorp.erp.modules.projects.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(DomainEvent event) {
        log.info("Publishing event: type={} targetUserId={} entityId={}", event.getType(), event.getTargetUserId(), event.getEntityId());
        applicationEventPublisher.publishEvent(event);
    }

    public void publishAsync(DomainEvent event) {
        log.info("Publishing async event: type={} targetUserId={}", event.getType(), event.getTargetUserId());
        applicationEventPublisher.publishEvent(event);
    }
}



