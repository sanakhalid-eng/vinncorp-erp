package com.vinncorp.erp.shared.websocket;

import com.vinncorp.erp.modules.projects.event.WebSocketEvent;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import java.security.Principal;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final PresenceService presenceService;
    private final TypingIndicatorService typingIndicatorService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        java.security.Principal principal = headerAccessor.getUser();

        if (principal != null && principal instanceof Authentication auth && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            Long userId = userDetails.getUserId();
            Long workspaceId = extractWorkspaceId(headerAccessor);

            if (workspaceId != null) {
                presenceService.userConnected(userId, workspaceId);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        java.security.Principal principal = headerAccessor.getUser();

        if (principal != null && principal instanceof Authentication auth && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            Long userId = userDetails.getUserId();
            Long workspaceId = extractWorkspaceId(headerAccessor);

            presenceService.userDisconnected(userId, workspaceId);
        }
    }

    @MessageMapping("/presence.update")
    public void handlePresenceUpdate(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null || !(principal instanceof Authentication auth) || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return;
        }

        Long userId = userDetails.getUserId();
        Long workspaceId = payload.get("workspaceId") != null ? Long.valueOf(payload.get("workspaceId").toString()) : null;

        if (workspaceId != null) {
            presenceService.userActivity(userId, workspaceId);
        }
    }

    @MessageMapping("/typing.start")
    public void handleTypingStart(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null || !(principal instanceof Authentication auth) || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return;
        }

        Long userId = userDetails.getUserId();
        String userName = userDetails.user().getName();
        Long workspaceId = payload.get("workspaceId") != null ? Long.valueOf(payload.get("workspaceId").toString()) : null;
        String entityType = payload.get("entityType") != null ? payload.get("entityType").toString() : null;
        Long entityId = payload.get("entityId") != null ? Long.valueOf(payload.get("entityId").toString()) : null;

        if (workspaceId != null && entityType != null && entityId != null) {
            typingIndicatorService.userTyping(userId, userName, workspaceId, entityType, entityId);
        }
    }

    @MessageMapping("/typing.stop")
    public void handleTypingStop(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null || !(principal instanceof Authentication auth) || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return;
        }

        Long userId = userDetails.getUserId();
        Long workspaceId = payload.get("workspaceId") != null ? Long.valueOf(payload.get("workspaceId").toString()) : null;
        String entityType = payload.get("entityType") != null ? payload.get("entityType").toString() : null;
        Long entityId = payload.get("entityId") != null ? Long.valueOf(payload.get("entityId").toString()) : null;

        if (workspaceId != null && entityType != null && entityId != null) {
            typingIndicatorService.userStoppedTyping(userId, workspaceId, entityType, entityId);
        }
    }

    @MessageMapping("/workspace.subscribe")
    public void handleWorkspaceSubscribe(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null || !(principal instanceof Authentication auth) || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return;
        }

        Long userId = userDetails.getUserId();
        Long workspaceId = payload.get("workspaceId") != null ? Long.valueOf(payload.get("workspaceId").toString()) : null;

        if (workspaceId != null) {
            Map<String, Object> presenceSummary = presenceService.getPresenceSummary(workspaceId);
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/presence",
                    WebSocketEvent.of("presence", "presence_summary", workspaceId, presenceSummary)
            );
        }
    }

    private Long extractWorkspaceId(StompHeaderAccessor headerAccessor) {
        String workspaceId = headerAccessor.getFirstNativeHeader("X-Workspace-Id");
        if (workspaceId != null) {
            try {
                return Long.valueOf(workspaceId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}



