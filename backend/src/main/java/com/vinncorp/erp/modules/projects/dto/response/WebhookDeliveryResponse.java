package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.entity.WebhookDelivery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDeliveryResponse {
    private Long id;
    private Long webhookId;
    private String eventType;
    private String payload;
    private Integer responseStatus;
    private String responseBody;
    private Integer retryCount;
    private String status;
    private LocalDateTime nextRetryAt;
    private LocalDateTime createdAt;

    public static WebhookDeliveryResponse fromEntity(WebhookDelivery delivery) {
        return new WebhookDeliveryResponse(
                delivery.getId(),
                delivery.getWebhook().getId(),
                delivery.getEventType(),
                delivery.getPayload(),
                delivery.getResponseStatus(),
                delivery.getResponseBody(),
                delivery.getRetryCount(),
                delivery.getStatus().name(),
                delivery.getNextRetryAt(),
                delivery.getCreatedAt()
        );
    }
}



