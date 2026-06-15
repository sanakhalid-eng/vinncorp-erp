package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.WebhookRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.WebhookDeliveryResponse;
import com.vinncorp.erp.modules.projects.dto.response.WebhookResponse;
import com.vinncorp.erp.modules.projects.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Webhooks")
public class WebhookController {
    private final WebhookService webhookService;

    @PreAuthorize("hasRole('ADMIN') or @securityService.isProjectManager(#projectId)")
    @PostMapping("/projects/{projectId}/webhooks")
    @Operation(summary = "Create webhook", description = "Create a new webhook for a project")
    public ResponseEntity<ApiResponse<WebhookResponse>> createWebhook(
            @PathVariable Long projectId,
            @RequestBody WebhookRequest request) {
        WebhookResponse webhook = webhookService.createWebhook(
                projectId, 
                request.getUrl(), 
                request.getSecret(), 
                request.getEvents()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Webhook created successfully", webhook));
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isProjectMember(#projectId)")
    @GetMapping("/projects/{projectId}/webhooks")
    @Operation(summary = "Get project webhooks", description = "Retrieve all webhooks for a project")
    public ResponseEntity<ApiResponse<List<WebhookResponse>>> getProjectWebhooks(@PathVariable Long projectId) {
        List<WebhookResponse> webhooks = webhookService.getProjectWebhooks(projectId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Webhooks fetched successfully", webhooks));
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isWebhookOwner(#webhookId)")
    @PutMapping("/webhooks/{webhookId}")
    @Operation(summary = "Update webhook", description = "Update an existing webhook configuration")
    public ResponseEntity<ApiResponse<WebhookResponse>> updateWebhook(
            @PathVariable Long webhookId,
            @RequestBody WebhookRequest request) {
        WebhookResponse webhook = webhookService.updateWebhook(
                webhookId,
                request.getUrl(),
                request.getIsActive(),
                request.getEvents()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Webhook updated successfully", webhook));
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isWebhookOwner(#webhookId)")
    @DeleteMapping("/webhooks/{webhookId}")
    @Operation(summary = "Delete webhook", description = "Delete a webhook by ID")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(@PathVariable Long webhookId) {
        webhookService.deleteWebhook(webhookId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Webhook deleted successfully", null));
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isWebhookOwner(#webhookId)")
    @GetMapping("/webhooks/{webhookId}/deliveries")
    @Operation(summary = "Get delivery logs", description = "Retrieve delivery logs for a webhook")
    public ResponseEntity<ApiResponse<List<WebhookDeliveryResponse>>> getDeliveryLogs(@PathVariable Long webhookId) {
        List<WebhookDeliveryResponse> deliveries = webhookService.getDeliveryLogs(webhookId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery logs fetched successfully", deliveries));
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isWebhookOwner(#webhookId)")
    @PostMapping("/webhooks/{webhookId}/test")
    @Operation(summary = "Send test event", description = "Send a test webhook event")
    public ResponseEntity<ApiResponse<Void>> sendTestEvent(@PathVariable Long webhookId) {
        webhookService.sendTestEvent(webhookId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Test event sent successfully", null));
    }
}



