package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.SlackIntegrationResponse;
import com.vinncorp.erp.modules.projects.entity.SlackIntegration;
import com.vinncorp.erp.modules.projects.repository.SlackIntegrationRepository;
import com.vinncorp.erp.modules.projects.service.SlackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/slack")
@RequiredArgsConstructor
@Tag(name = "Slack")
public class SlackController {
    private final SlackService slackService;
    private final SlackIntegrationRepository slackIntegrationRepository;

    @GetMapping("/oauth/url")
    @Operation(summary = "Get Slack OAuth URL", description = "Generate Slack OAuth URL for integration")
    public ResponseEntity<ApiResponse<Map<String, String>>> getOAuthUrl(
            @RequestParam Long projectId,
            @RequestParam(required = false) String workspaceId) {
        String url = slackService.getOAuthUrl(projectId.toString(), workspaceId);
        return ResponseEntity.ok(new ApiResponse<>(true, "OAuth URL generated", Map.of("url", url)));
    }

    @PostMapping("/oauth/callback")
    @Operation(summary = "Handle Slack OAuth callback", description = "Complete Slack OAuth flow with authorization code")
    public ResponseEntity<ApiResponse<SlackIntegrationResponse>> handleOAuthCallback(@RequestParam String code, @RequestParam String state) {
        SlackIntegration integration = slackService.handleOAuthCallback(code, state);
        return ResponseEntity.ok(new ApiResponse<>(true, "Slack integration successful", SlackIntegrationResponse.fromEntity(integration)));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Handle Slack webhook", description = "Process incoming Slack webhook events and interactive buttons")
    public ResponseEntity<ApiResponse<Void>> handleSlackWebhook(HttpServletRequest request) {
        try {
            // Read raw body for signature verification
            String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String timestamp = request.getHeader("X-Slack-Request-Timestamp");
            String signature = request.getHeader("X-Slack-Signature");

            if (!slackService.verifySignature(rawBody, timestamp, signature)) {
                return ResponseEntity.status(401).body(new ApiResponse<>(false, "Invalid Slack signature", null));
            }

            // Extract JSON payload from the raw body
            String jsonPayload;
            if (rawBody.startsWith("payload=")) {
                String decodedBody = java.net.URLDecoder.decode(rawBody, StandardCharsets.UTF_8);
                jsonPayload = decodedBody.substring(decodedBody.indexOf("payload=") + 8);
            } else {
                jsonPayload = rawBody;
            }

            slackService.handleInteractiveButton(jsonPayload);
            return ResponseEntity.ok(new ApiResponse<>(true, "Webhook processed", null));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false, "Invalid Slack signature", null));
        }
    }

    @GetMapping("/integration")
    @Operation(summary = "Get Slack integration", description = "Retrieve Slack integration for a project")
    public ResponseEntity<ApiResponse<SlackIntegrationResponse>> getIntegration(@RequestParam Long projectId) {
        return slackIntegrationRepository.findByProjectIdAndIsActiveTrue(projectId)
                .stream()
                .findFirst()
                .map(integration -> ResponseEntity.ok(new ApiResponse<>(true, "Integration found", SlackIntegrationResponse.fromEntity(integration))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("No integration found", 404)));
    }

    @DeleteMapping("/integration/{id}")
    @Operation(summary = "Remove Slack integration", description = "Remove a Slack integration by ID")
    public ResponseEntity<ApiResponse<Void>> removeIntegration(@PathVariable Long id) {
        slackIntegrationRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Integration removed", null));
    }

    @PostMapping("/sync-users/{workspaceId}")
    @Operation(summary = "Sync Slack users", description = "Synchronize user mappings from Slack workspace")
    public ResponseEntity<ApiResponse<Void>> syncUsers(@PathVariable String workspaceId) {
        slackService.syncUserMappings(workspaceId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Users synced successfully", null));
    }
}



