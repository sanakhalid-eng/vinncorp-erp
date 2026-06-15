package com.vinncorp.erp.core.auth.repository;

import com.vinncorp.erp.core.auth.entity.UserTwoFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TwoFactorRepository extends JpaRepository<UserTwoFactor, Long> {
    Optional<UserTwoFactor> findByUserId(Long userId);
    boolean existsByUserIdAndEnabledTrue(Long userId);
}
