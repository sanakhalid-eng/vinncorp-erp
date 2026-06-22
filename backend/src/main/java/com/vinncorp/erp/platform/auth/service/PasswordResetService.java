package com.vinncorp.erp.platform.auth.service;

public interface PasswordResetService {
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
    boolean verifyToken(String token);
}

