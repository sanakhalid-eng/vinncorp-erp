package com.vinncorp.erp.modules.projects.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketEvent<T> {

    private String type;
    private String event;
    private Long workspaceId;
    private String entityType;
    private Long entityId;
    private T data;
    private Map<String, Object> metadata;
    private String userId;
    private String userName;
    private LocalDateTime timestamp;

    public static <T> WebSocketEvent<T> of(String type, String event, Long workspaceId) {
        return WebSocketEvent.<T>builder()
                .type(type)
                .event(event)
                .workspaceId(workspaceId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> WebSocketEvent<T> of(String type, String event, Long workspaceId, T data) {
        return WebSocketEvent.<T>builder()
                .type(type)
                .event(event)
                .workspaceId(workspaceId)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> WebSocketEvent<T> of(String type, String event, Long workspaceId, String entityType, Long entityId, T data) {
        return WebSocketEvent.<T>builder()
                .type(type)
                .event(event)
                .workspaceId(workspaceId)
                .entityType(entityType)
                .entityId(entityId)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}



