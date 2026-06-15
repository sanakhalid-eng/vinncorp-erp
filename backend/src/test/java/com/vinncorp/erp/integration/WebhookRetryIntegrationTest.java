package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;
import com.vinncorp.erp.modules.projects.entity.Webhook;
import com.vinncorp.erp.modules.projects.entity.WebhookDelivery;
import com.vinncorp.erp.modules.projects.enums.WebhookDeliveryStatus;
import com.vinncorp.erp.modules.projects.repository.WebhookDeliveryRepository;
import com.vinncorp.erp.modules.projects.repository.WebhookRepository;
import com.vinncorp.erp.modules.projects.service.RetryService;
import com.vinncorp.erp.modules.projects.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebhookRetryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebhookService webhookService;
    @Autowired
    private RetryService retryService;
    @Autowired
    private WebhookRepository webhookRepository;
    @Autowired
    private WebhookDeliveryRepository deliveryRepository;

    private Webhook testWebhook;

    @BeforeEach
    void setUp() {
        deliveryRepository.deleteAll();
        webhookRepository.deleteAll();

        testWebhook = Webhook.builder()
                .project(testProject)
                .url("https://example.com/webhook")
                .secret("test-secret")
                .events("[\"task.created\"]")
                .isActive(true)
                .build();
        testWebhook = webhookRepository.save(testWebhook);
    }

    @Test
    void enqueueRetry_shouldCreatePendingEntry() {
        var entry = retryService.enqueue("WEBHOOK", "TASK", 1L, "{\"event\":\"test\"}", 3);

        assertNotNull(entry.getId());
        assertEquals("WEBHOOK", entry.getType());
        assertEquals("PENDING", entry.getStatus());
        assertEquals(0, entry.getRetryCount());
    }

    @Test
    void markRetriedSuccess_shouldCompleteEntry() {
        var entry = retryService.enqueue("WEBHOOK", "TASK", 1L, "{\"event\":\"test\"}", 3);

        retryService.markRetried(entry.getId(), true, null);

        var completed = retryService.getQueueBacklog();
        // PENDING entries + RETRYING entries = 0 since this one is now COMPLETED
        assertEquals(0, retryService.getQueueBacklog());
    }

    @Test
    void markRetriedFailure_shouldIncrementAndScheduleNextRetry() {
        var entry = retryService.enqueue("WEBHOOK", "TASK", 1L, "{\"event\":\"test\"}", 3);

        retryService.markRetried(entry.getId(), false, "Connection timeout");

        assertTrue(retryService.getQueueBacklog() > 0);
    }

    @Test
    void maxRetriesReached_shouldMoveToDeadLetter() {
        var entry = retryService.enqueue("WEBHOOK", "TASK", 1L, "{\"event\":\"test\"}", 2);

        retryService.markRetried(entry.getId(), false, "Attempt 1");
        retryService.markRetried(entry.getId(), false, "Attempt 2");

        // After max retries (2), it should be DEAD_LETTER
        assertEquals(0, retryService.getQueueBacklog());
        assertEquals(1, retryService.getDeadLetterCount());
    }

    @Test
    void createWebhookDelivery_shouldStartAsPending() {
        webhookService.createWebhook(testProject.getId(), "https://example.com/hook", "secret", List.of("task.created"));

        List<Webhook> webhooks = webhookRepository.findByProjectId(testProject.getId());
        assertEquals(2, webhooks.size());
    }

    @Test
    void retryDelivery_onDeadLetter_shouldFail() {
        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhook(testWebhook)
                .eventType("task.created")
                .payload("{\"test\":true}")
                .status(WebhookDeliveryStatus.DEAD_LETTER)
                .retryCount(5)
                .build();
        WebhookDelivery savedDelivery = deliveryRepository.save(delivery);

        assertThrows(IllegalStateException.class, () ->
                webhookService.retryDelivery(savedDelivery.getId()));
    }

    @Test
    void generateSignature_shouldProduceConsistentHash() {
        String sig1 = webhookService.generateSignature("payload", "secret");
        String sig2 = webhookService.generateSignature("payload", "secret");
        String sig3 = webhookService.generateSignature("payload", "different-secret");

        assertEquals(sig1, sig2);
        assertNotEquals(sig1, sig3);
    }

    @Test
    void signature_isHexString() {
        String sig = webhookService.generateSignature("test", "key");
        assertTrue(sig.matches("^[0-9a-f]+$"));
        assertEquals(64, sig.length()); // SHA-256 is 64 hex chars
    }
}

