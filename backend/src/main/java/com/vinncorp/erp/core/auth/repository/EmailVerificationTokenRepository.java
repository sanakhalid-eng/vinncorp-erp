package com.vinncorp.erp.core.auth.repository;

import com.vinncorp.erp.core.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenAndEmailAndIsUsedFalse(String token, String email);
    List<EmailVerificationToken> findByEmailAndIsUsedFalse(String email);
    List<EmailVerificationToken> findByUserIdAndIsUsedFalseOrderByCreatedAtDesc(Long userId);
    int deleteByExpiryDateBefore(LocalDateTime expiryDate);

}

