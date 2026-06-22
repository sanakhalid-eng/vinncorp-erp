package com.vinncorp.erp.platform.notification.service;
import com.vinncorp.erp.platform.notification.dto.response.NotificationIntelligenceResponse;

public interface NotificationIntelligenceService {
NotificationIntelligenceResponse getIntelligence(Long userId);
} 