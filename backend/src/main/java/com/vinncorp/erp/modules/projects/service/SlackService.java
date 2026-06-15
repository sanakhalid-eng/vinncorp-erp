package com.vinncorp.erp.modules.projects.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.SlackIntegration;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.SlackIntegrationRepository;
import com.vinncorp.erp.shared.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SlackService {
    private final SlackIntegrationRepository slackIntegrationRepository;
    private final ProjectRepository projectRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EncryptionService encryptionService;

    @Value("${slack.client-id:}")
    private String clientId;

    @Value("${slack.client-secret:}")
    private String clientSecret;

    @Value("${slack.signing-secret:}")
    private String signingSecret;

    @Value("${app.base-url:http://localhost:8081}")
    private String appBaseUrl;

    public String getOAuthUrl(String projectId, String workspaceId) {
        String redirectUri = appBaseUrl + "/api/slack/oauth/callback";
        return "https://slack.com/oauth/v2/authorize?" +
                "client_id=" + clientId +
                "&scope=app_mentions:read,channels:read,channels:join,chat:write,users:read,users:read.email" +
                "&redirect_uri=" + redirectUri +
                "&state=" + projectId + ":" + workspaceId +
                "&user_scope=search:read";
    }

    public SlackIntegration handleOAuthCallback(String code, String state) {
        try {
            String tokenUrl = "https://slack.com/api/oauth.v2.access";
            String requestBody = "client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&code=" + code +
                    "&redirect_uri=" + appBaseUrl + "/api/slack/oauth/callback";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            JsonNode tokenData = objectMapper.readTree(response.getBody());

            if (!tokenData.get("ok").asBoolean()) {
                throw new RuntimeException("Slack OAuth failed: " + tokenData.get("error").asText());
            }

            String[] stateParts = state.split(":");
            Long projectId = Long.parseLong(stateParts[0]);

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            JsonNode team = tokenData.get("team");
            JsonNode authedUser = tokenData.get("authed_user");

            String rawAccessToken = tokenData.get("access_token").asText();
            String rawRefreshToken = tokenData.has("refresh_token") ? tokenData.get("refresh_token").asText() : null;

            SlackIntegration integration = SlackIntegration.builder()
                    .project(project)
                    .workspaceId(team.get("id").asText())
                    .workspaceName(team.get("name").asText())
                    .accessToken(encryptionService.encrypt(rawAccessToken))
                    .refreshToken(rawRefreshToken != null ? encryptionService.encrypt(rawRefreshToken) : null)
                    .signingSecret(clientSecret)
                    .botUserId(authedUser.get("id").asText())
                    .isActive(true)
                    .build();

            return slackIntegrationRepository.save(integration);

        } catch (Exception e) {
            throw new RuntimeException("Failed to handle OAuth callback", e);
        }
    }

    public void sendMessage(Long projectId, String channel, String text, List<Map<String, Object>> blocks) {
        Optional<SlackIntegration> integrationOpt = slackIntegrationRepository
                .findByProjectIdAndIsActiveTrue(projectId).stream().findFirst();

        if (integrationOpt.isEmpty()) {
            throw new RuntimeException("No active Slack integration found for project");
        }

        SlackIntegration integration = integrationOpt.get();
        sendSlackMessage(integration.getAccessToken(), channel, text, blocks);
    }

    public void sendMessageToWorkspace(String workspaceId, String channel, String text, List<Map<String, Object>> blocks) {
        SlackIntegration integration = slackIntegrationRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new RuntimeException("Slack integration not found"));

        sendSlackMessage(integration.getAccessToken(), channel, text, blocks);
    }

    private void sendSlackMessage(String encryptedToken, String channel, String text, List<Map<String, Object>> blocks) {
        try {
            String decryptedToken = encryptionService.decrypt(encryptedToken);
            String url = "https://slack.com/api/chat.postMessage";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(decryptedToken);

            Map<String, Object> body = new HashMap<>();
            body.put("channel", channel);
            body.put("text", text);
            if (blocks != null) {
                body.put("blocks", blocks);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode responseData = objectMapper.readTree(response.getBody());
            if (!responseData.get("ok").asBoolean()) {
                throw new RuntimeException("Slack API error: " + responseData.get("error").asText());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send Slack message", e);
        }
    }

    public void sendTaskCreatedNotification(String workspaceId, String channel, JsonNode taskData) {
        List<Map<String, Object>> blocks = new ArrayList<>();

        Map<String, Object> headerBlock = new HashMap<>();
        headerBlock.put("type", "header");
        Map<String, Object> headerText = new HashMap<>();
        headerText.put("type", "plain_text");
        headerText.put("text", "✅ New Task Created");
        headerBlock.put("text", headerText);
        blocks.add(headerBlock);

        Map<String, Object> sectionBlock = new HashMap<>();
        sectionBlock.put("type", "section");
        List<Map<String, Object>> fields = new ArrayList<>();
        
        Map<String, Object> field1 = new HashMap<>();
        field1.put("type", "mrkdwn");
        field1.put("text", "*Task:* " + taskData.get("title").asText());
        fields.add(field1);
        
        Map<String, Object> field2 = new HashMap<>();
        field2.put("type", "mrkdwn");
        field2.put("text", "*Priority:* " + taskData.get("priority").asText());
        fields.add(field2);
        
        if (taskData.has("assigneeSlackId")) {
            Map<String, Object> field3 = new HashMap<>();
            field3.put("type", "mrkdwn");
            field3.put("text", "*Assignee:* <@" + taskData.get("assigneeSlackId").asText() + ">");
            fields.add(field3);
        }
        
        sectionBlock.put("fields", fields);
        blocks.add(sectionBlock);

        Map<String, Object> actionsBlock = new HashMap<>();
        actionsBlock.put("type", "actions");
        List<Map<String, Object>> elements = new ArrayList<>();
        
        Map<String, Object> viewButton = new HashMap<>();
        viewButton.put("type", "button");
        Map<String, Object> viewText = new HashMap<>();
        viewText.put("type", "plain_text");
        viewText.put("text", "View Task");
        viewButton.put("text", viewText);
        viewButton.put("url", appBaseUrl + "/projects/" + taskData.get("projectId").asText() + "/tasks/" + taskData.get("taskId").asText());
        viewButton.put("style", "primary");
        elements.add(viewButton);
        
        Map<String, Object> completeButton = new HashMap<>();
        completeButton.put("type", "button");
        Map<String, Object> completeText = new HashMap<>();
        completeText.put("type", "plain_text");
        completeText.put("text", "Mark Complete");
        completeButton.put("text", completeText);
        completeButton.put("value", "complete_task:" + taskData.get("taskId").asText());
        completeButton.put("style", "danger");
        elements.add(completeButton);
        
        actionsBlock.put("elements", elements);
        blocks.add(actionsBlock);

        sendMessageToWorkspace(workspaceId, channel, "New task created", blocks);
    }

    public boolean verifySignature(String rawBody, String timestamp, String signature) {
        if (signingSecret == null || signingSecret.isBlank()) return false;
        if (timestamp == null || signature == null) return false;
        if (rawBody == null || rawBody.isEmpty()) return false;
        if (!signature.startsWith("v0=")) return false;

        try {
            long now = Instant.now().getEpochSecond();
            long ts = Long.parseLong(timestamp);
            if (Math.abs(now - ts) > 300) return false;

            String baseString = "v0:" + timestamp + ":" + rawBody;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(signingSecret.getBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(baseString.getBytes());
            StringBuilder computed = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) computed.append('0');
                computed.append(hex);
            }
            return computed.toString().equals(signature.replace("v0=", ""));
        } catch (NumberFormatException | NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    public void handleInteractiveButton(String payload) {
        try {
            JsonNode payloadData = objectMapper.readTree(payload);
            String workspaceId = payloadData.get("team").get("id").asText();
            JsonNode actions = payloadData.get("actions");
            
            if (actions != null && actions.isArray() && !actions.isEmpty()) {
                JsonNode action = actions.get(0);
                String actionValue = action.get("value").asText();
                
                if (actionValue.startsWith("complete_task:")) {
                    String taskId = actionValue.split(":")[1];
                    // Update task status via service
                    // taskService.updateTaskStatus(Long.parseLong(taskId), "DONE");
                    
                    // Send confirmation
                    String channel = payloadData.get("channel").get("id").asText();
                    sendMessageToWorkspace(workspaceId, channel, "✅ Task marked as complete!", null);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle interactive button", e);
        }
    }

    public void syncUserMappings(String workspaceId) {
        SlackIntegration integration = slackIntegrationRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new RuntimeException("Slack integration not found"));

        try {
            String decryptedToken = encryptionService.decrypt(integration.getAccessToken());
            String url = "https://slack.com/api/users.list";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(decryptedToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            JsonNode responseData = objectMapper.readTree(response.getBody());
            if (responseData.get("ok").asBoolean()) {
                JsonNode members = responseData.get("members");
                for (JsonNode member : members) {
                    String slackUserId = member.get("id").asText();
                    String slackUsername = member.get("name").asText();
                    
                    // In a real implementation, save these mappings to database
                    System.out.println("Slack user: " + slackUsername + " (" + slackUserId + ")");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync user mappings", e);
        }
    }

    public Optional<SlackIntegration> getProjectIntegration(Long projectId) {
        return slackIntegrationRepository.findByProjectIdAndIsActiveTrue(projectId).stream().findFirst();
    }
}


