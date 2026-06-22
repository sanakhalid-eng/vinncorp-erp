package com.vinncorp.erp.platform.notification.dto.response;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;
@Data
@Builder

public class NotificationIntelligenceResponse {
private long unreadCount;
private Map<String, Long> categoryBreakdown;
private List<String> suggestedActions;
private boolean digestRecommended;
} 