package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.entity.Webhook;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {
    private Long id;
    private Long projectId;
    private String url;
    private String secret;
    private String events;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static WebhookResponse fromEntity(Webhook webhook) {
        return new WebhookResponse(
                webhook.getId(),
                webhook.getProject().getId(),
                webhook.getUrl(),
                maskSecret(webhook.getSecret()),
                webhook.getEvents(),
                webhook.getIsActive(),
                webhook.getCreatedAt()
        );
    }

    private static String maskSecret(String secret) {
        if (secret == null) return null;
        if (secret.length() <= 4) return "****";
        return secret.substring(0, 2) + "****" + secret.substring(secret.length() - 2);
    }
}



