package com.vinncorp.erp.core.auth.service;

public interface PasswordResetService {
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
    boolean verifyToken(String token);
}

