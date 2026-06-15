package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.modules.projects.entity.EmailDelivery;
import com.vinncorp.erp.modules.projects.repository.EmailDeliveryRepository;
import com.vinncorp.erp.modules.projects.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailDeliveryServiceImpl {
    private final EmailDeliveryRepository emailDeliveryRepository;
    private final EmailService emailService;

    @Transactional
    public EmailDelivery trackAndSend(String recipientEmail, String subject, String emailType, String htmlContent, String plainText, String entityType, Long entityId) {
        EmailDelivery delivery = new EmailDelivery();
        delivery.setRecipientEmail(recipientEmail);
        delivery.setSubject(subject);
        delivery.setEmailType(emailType);
        delivery.setEntityType(entityType);
        delivery.setEntityId(entityId);
        delivery = emailDeliveryRepository.save(delivery);

        try {
            emailService.sendSimpleEmailWithFallback(recipientEmail, subject, htmlContent, plainText);
            delivery.setStatus("SENT");
            delivery.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            delivery.setRetryCount(delivery.getRetryCount() + 1);
            delivery.setLastError(e.getMessage());
            if (delivery.getRetryCount() >= delivery.getMaxRetries()) {
                delivery.setStatus("FAILED");
                log.error("Email delivery {} permanently failed after {} retries: {}", delivery.getId(), delivery.getMaxRetries(), e.getMessage());
            } else {
                delivery.setStatus("RETRYING");
                log.warn("Email delivery {} failed, will retry ({}/{}): {}", delivery.getId(), delivery.getRetryCount(), delivery.getMaxRetries(), e.getMessage());
            }
        }

        return emailDeliveryRepository.save(delivery);
    }

    public long getFailedEmailCount() {
        return emailDeliveryRepository.countByStatus("FAILED");
    }

    public long getPendingEmailCount() {
        return emailDeliveryRepository.countByStatus("PENDING") + emailDeliveryRepository.countByStatus("RETRYING");
    }
}



