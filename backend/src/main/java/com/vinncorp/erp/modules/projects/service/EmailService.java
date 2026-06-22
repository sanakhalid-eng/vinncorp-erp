package com.vinncorp.erp.modules.projects.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender, EmailTemplateService emailTemplateService) {
        this.mailSender = mailSender;
        this.emailTemplateService = emailTemplateService;
    }

    @Async
    public void sendVerificationEmail(String toEmail, String code) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Verify Your Email - PMT-SK");

            Map<String, Object> variables = new HashMap<>();
            variables.put("code", code);
            
            String htmlContent = emailTemplateService.loadTemplate(
                "verification-code.html",
                variables
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Verification email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send verification email to {}", toEmail, e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - PMT-SK");

            String resetUrl = baseUrl + "/reset-password?token=" + token;

            Map<String, Object> variables = new HashMap<>();
            variables.put("resetUrl", resetUrl);
            
            String htmlContent = emailTemplateService.loadTemplate(
                "password-reset.html",
                variables
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Password reset email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }

    public void sendSimpleEmailWithFallback(String toEmail, String subject, String htmlBody, String plainText) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);

            String fullHtml = "<!DOCTYPE html>" +
                    "<html><head><meta charset='UTF-8'></head>" +
                    "<body style='font-family: Arial, sans-serif; background-color:#f9fafb; padding:20px;'>" +
                    "<div style='max-width:600px; margin:0 auto; background-color:white; padding:30px; border-radius:10px;'>" +
                    htmlBody +
                    "<hr style='margin:30px 0; border:none; border-top:1px solid #e5e7eb;'>" +
                    "<p style='color:#6b7280; font-size:12px; text-align:center;'>© 2026 PMT-SK. All rights reserved.</p>" +
                    "</div></body></html>";

            helper.setText(fullHtml, plainText);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // Simple email method for notifications (no template needed)
    public void sendSimpleEmail(String toEmail, String subject, String htmlBody) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Wrap in basic HTML structure
            String fullHtml = "<!DOCTYPE html>" +
                    "<html><head><meta charset='UTF-8'></head>" +
                    "<body style='font-family: Arial, sans-serif; background-color:#f9fafb; padding:20px;'>" +
                    "<div style='max-width:600px; margin:0 auto; background-color:white; padding:30px; border-radius:10px;'>" +
                    htmlBody +
                    "<hr style='margin:30px 0; border:none; border-top:1px solid #e5e7eb;'>" +
                    "<p style='color:#6b7280; font-size:12px; text-align:center;'>© 2026 PMT-SK. All rights reserved.</p>" +
                    "</div></body></html>";

            helper.setText(fullHtml, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send notification email", e);
        }
    }
}


