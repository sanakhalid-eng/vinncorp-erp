package com.vinncorp.erp.modules.projects.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.modules.projects.dto.response.WebhookDeliveryResponse;
import com.vinncorp.erp.modules.projects.dto.response.WebhookResponse;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.Webhook;
import com.vinncorp.erp.modules.projects.entity.WebhookDelivery;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.enums.WebhookDeliveryStatus;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.WebhookDeliveryRepository;
import com.vinncorp.erp.modules.projects.repository.WebhookRepository;
import com.vinncorp.erp.shared.security.WebhookUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;
    private final WebhookUrlValidator urlValidator;
    private final ActivityLogService activityLogService;

    @Qualifier("webhookRestTemplate")
    private final RestTemplate webhookRestTemplate;

    @Async
    public void publishEvent(String eventType, Long projectId, Object eventData) {
        List<Webhook> webhooks = webhookRepository.findByProjectIdAndIsActiveTrue(projectId);

        for (Webhook webhook : webhooks) {
            try {
                List<String> subscribedEvents = objectMapper.readValue(webhook.getEvents(), List.class);
                if (!subscribedEvents.contains(eventType)) {
                    continue;
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("event", eventType);
                payload.put("timestamp", LocalDateTime.now().toString());
                payload.put("projectId", projectId);
                payload.put("data", eventData);

                String payloadJson = objectMapper.writeValueAsString(payload);
                String signature = generateSignature(payloadJson, webhook.getSecret());

                WebhookDelivery delivery = WebhookDelivery.builder()
                        .webhook(webhook)
                        .eventType(eventType)
                        .payload(payloadJson)
                        .status(WebhookDeliveryStatus.PENDING)
                        .retryCount(0)
                        .build();

                delivery = deliveryRepository.save(delivery);
                deliverWebhook(delivery.getId(), webhook.getUrl(), payloadJson, signature);

            } catch (Exception e) {
                log.error("Failed to publish event {} for webhook in project {}", eventType, projectId, e);
            }
        }
    }

    @Async
    public void deliverWebhook(Long deliveryId, String url, String payload, String signature) {
        try {
            // Validate URL before each delivery attempt
            try {
                urlValidator.validate(url);
            } catch (IllegalArgumentException e) {
                activityLogService.logActivity(
                        null, EntityType.SYSTEM, deliveryId, ActionType.WEBHOOK_BLOCKED,
                        null, Map.of("reason", e.getMessage(), "url", url),
                        "Webhook delivery blocked: " + e.getMessage(), null
                );
                markDeadLetter(deliveryId, e.getMessage());
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Signature", signature);
            headers.set("X-Event-Type", objectMapper.readTree(payload).get("event").asText());

            // Enforce payload size limit (1MB)
            if (payload.length() > 1_048_576) {
                markDeadLetter(deliveryId, "Payload exceeds maximum size of 1MB");
                return;
            }

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = webhookRestTemplate.postForEntity(url, entity, String.class);

            WebhookDelivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
            delivery.setResponseStatus(response.getStatusCode().value());
            delivery.setResponseBody(truncateResponseBody(response.getBody()));
            delivery.setStatus(WebhookDeliveryStatus.SUCCESS);
            deliveryRepository.save(delivery);

        } catch (Exception e) {
            handleDeliveryFailure(deliveryId, e);
        }
    }

    private void handleDeliveryFailure(Long deliveryId, Exception e) {
        deliveryRepository.findById(deliveryId).ifPresent(delivery -> {
            int currentRetries = delivery.getRetryCount() + 1;
            delivery.setRetryCount(currentRetries);

            if (currentRetries >= 5) {
                delivery.setStatus(WebhookDeliveryStatus.DEAD_LETTER);
                log.warn("Webhook delivery {} reached max retries, moving to DEAD_LETTER", deliveryId);
            } else {
                delivery.setStatus(WebhookDeliveryStatus.RETRYING);
                delivery.setNextRetryAt(calculateNextRetryTime(currentRetries));
            }

            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 1000) {
                errorMsg = errorMsg.substring(0, 1000);
            }
            delivery.setResponseBody(errorMsg);
            deliveryRepository.save(delivery);
        });
    }

    private void markDeadLetter(Long deliveryId, String reason) {
        deliveryRepository.findById(deliveryId).ifPresent(delivery -> {
            delivery.setStatus(WebhookDeliveryStatus.DEAD_LETTER);
            delivery.setResponseBody(reason);
            deliveryRepository.save(delivery);
            log.warn("Webhook delivery {} marked as DEAD_LETTER: {}", deliveryId, reason);
        });
    }

    @Transactional
    public void retryDelivery(Long deliveryId) {
        WebhookDelivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        if (delivery.getStatus() == WebhookDeliveryStatus.DEAD_LETTER) {
            throw new IllegalStateException("Cannot retry a DEAD_LETTER delivery");
        }
        String signature = generateSignature(delivery.getPayload(), delivery.getWebhook().getSecret());
        deliverWebhook(delivery.getId(), delivery.getWebhook().getUrl(), delivery.getPayload(), signature);
    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {
        int minutes = switch (retryCount) {
            case 1 -> 1;
            case 2 -> 5;
            case 3 -> 15;
            case 4 -> 60;
            case 5 -> 360;
            default -> 1;
        };
        return LocalDateTime.now().plusMinutes(minutes);
    }

    public String generateSignature(String payload, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    secret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }

    @Transactional
    public WebhookResponse createWebhook(Long projectId, String url, String secret, List<String> events) {
        urlValidator.validate(url);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Webhook webhook = Webhook.builder()
                .project(project)
                .url(url)
                .secret(secret)
                .events(events.toString())
                .isActive(true)
                .build();
        webhook = webhookRepository.save(webhook);
        return WebhookResponse.fromEntity(webhook);
    }

    @Transactional(readOnly = true)
    public List<WebhookResponse> getProjectWebhooks(Long projectId) {
        return webhookRepository.findByProjectId(projectId).stream()
                .map(WebhookResponse::fromEntity)
                .toList();
    }

    @Transactional
    public WebhookResponse updateWebhook(Long webhookId, String url, Boolean isActive, List<String> events) {
        return webhookRepository.findById(webhookId)
                .map(webhook -> {
                    if (url != null) {
                        urlValidator.validate(url);
                        webhook.setUrl(url);
                    }
                    if (isActive != null) webhook.setIsActive(isActive);
                    if (events != null) webhook.setEvents(events.toString());
                    return WebhookResponse.fromEntity(webhookRepository.save(webhook));
                })
                .orElseThrow(() -> new RuntimeException("Webhook not found"));
    }

    @Transactional
    public void deleteWebhook(Long webhookId) {
        webhookRepository.deleteById(webhookId);
    }

    @Transactional(readOnly = true)
    public List<WebhookDeliveryResponse> getDeliveryLogs(Long webhookId) {
        return deliveryRepository.findByWebhookIdOrderByCreatedAtDesc(webhookId).stream()
                .map(WebhookDeliveryResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void sendTestEvent(Long webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new RuntimeException("Webhook not found"));

        Map<String, Object> testData = new HashMap<>();
        testData.put("message", "This is a test event from PMT-SK");
        testData.put("webhookId", webhookId);

        publishEvent("TEST_EVENT", webhook.getProject().getId(), testData);
    }

    private String truncateResponseBody(String body) {
        if (body != null && body.length() > 5000) {
            return body.substring(0, 5000) + "... (truncated)";
        }
        return body;
    }
}



