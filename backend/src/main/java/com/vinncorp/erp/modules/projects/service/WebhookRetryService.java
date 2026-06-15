package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.enums.WebhookDeliveryStatus;
import com.vinncorp.erp.modules.projects.repository.WebhookDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebhookRetryService {
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookService webhookService;

    @Scheduled(fixedDelay = 60000) // Run every minute
    @Transactional
    public void retryFailedDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        List<WebhookDeliveryStatus> statuses = List.of(WebhookDeliveryStatus.RETRYING, WebhookDeliveryStatus.FAILED);
        
        statuses.forEach(status -> {
            deliveryRepository.findByStatusAndNextRetryAtLessThanEqual(status, now)
                    .forEach(delivery -> {
                        if (delivery.getRetryCount() >= 5) {
                            delivery.setStatus(WebhookDeliveryStatus.DEAD_LETTER);
                            deliveryRepository.save(delivery);
                        } else if (delivery.getStatus() == WebhookDeliveryStatus.DEAD_LETTER) {
                            // Skip dead-letter deliveries
                        } else {
                            webhookService.retryDelivery(delivery.getId());
                        }
                    });
        });
    }
}



