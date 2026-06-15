package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.WebhookDelivery;
import com.vinncorp.erp.modules.projects.enums.WebhookDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    List<WebhookDelivery> findByWebhookIdOrderByCreatedAtDesc(Long webhookId);
    List<WebhookDelivery> findByStatusAndNextRetryAtLessThanEqual(WebhookDeliveryStatus status, LocalDateTime dateTime);
    List<WebhookDelivery> findByStatus(WebhookDeliveryStatus status);

    long countByStatus(WebhookDeliveryStatus status);
}



