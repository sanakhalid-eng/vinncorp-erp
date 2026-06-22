package com.vinncorp.erp.platform.notification.service.impl;
import com.vinncorp.erp.platform.notification.dto.response.NotificationIntelligenceResponse;
import com.vinncorp.erp.platform.notification.service.NotificationIntelligenceService;
import com.vinncorp.erp.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor 
public class NotificationIntelligenceServiceImpl implements NotificationIntelligenceService {
private final NotificationService notificationService;
@Override @Transactional(readOnly = true) 
public NotificationIntelligenceResponse getIntelligence(Long userId) {
Map<String, Long> breakdown = notificationService.getUnreadCountByCategory(userId);
long unread = breakdown.values().stream().mapToLong(Long::longValue).sum();
List<String> suggested = new ArrayList<>();
if (breakdown.getOrDefault("MENTIONS", 0L) > 0) {
suggested.add("Review pending mentions");
} if (breakdown.getOrDefault("ASSIGNMENTS", 0L) > 0) {
suggested.add("Triage new task assignments");
} if (breakdown.getOrDefault("DEADLINES", 0L) > 0) {
suggested.add("Check upcoming deadlines");
} if (suggested.isEmpty() && unread > 0) {
suggested.add("Clear unread notifications");
} boolean digestRecommended = unread >= 10;
return NotificationIntelligenceResponse.builder() .unreadCount(unread) .categoryBreakdown(breakdown) .suggestedActions(suggested) .digestRecommended(digestRecommended) .build();
}} 