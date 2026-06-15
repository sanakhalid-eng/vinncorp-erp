package com.vinncorp.erp.core.auth.repository;

import com.vinncorp.erp.core.auth.entity.RefreshToken;
import com.vinncorp.erp.core.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP")
    List<RefreshToken> findValidByUserId(@Param("userId") Long userId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenFamily = :tokenFamily AND rt.revoked = false")
    List<RefreshToken> findValidByTokenFamily(@Param("tokenFamily") String tokenFamily);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.replacedByToken = :newToken WHERE rt.token = :oldToken")
    void markReplaced(@Param("oldToken") String oldToken, @Param("newToken") String newToken);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    void revokeByToken(@Param("token") String token);

    void deleteByUser(User user);
}
