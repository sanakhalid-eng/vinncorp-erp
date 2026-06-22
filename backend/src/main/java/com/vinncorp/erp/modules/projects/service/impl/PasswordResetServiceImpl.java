package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.platform.auth.entity.PasswordResetToken;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.shared.exception.InvalidTokenException;
import com.vinncorp.erp.platform.auth.repository.PasswordResetTokenRepository;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.auth.service.PasswordResetService;
import com.vinncorp.erp.modules.projects.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        passwordResetTokenRepository.deleteAllByEmail(email);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .email(email)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(email, token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndIsUsedFalse(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Reset token has expired");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    public boolean verifyToken(String token) {
        return passwordResetTokenRepository.findByTokenAndIsUsedFalse(token)
                .map(resetToken -> !resetToken.isExpired())
                .orElse(false);
    }
}



