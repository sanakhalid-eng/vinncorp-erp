package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final WebhookRepository webhookRepository;

    public boolean isCurrentUser(Long userId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        var principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId().equals(userId);
        }
        return false;
    }

    public boolean isWebhookOwner(Long webhookId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        var principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return webhookRepository.findById(webhookId)
                    .map(webhook -> webhook.getProject().getMembers().stream()
                            .anyMatch(member -> member.getUser().getId().equals(userDetails.getUserId())))
                    .orElse(false);
        }
        return false;
    }
}



