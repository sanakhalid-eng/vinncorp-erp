package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.BootstrapLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BootstrapLockRepository extends JpaRepository<BootstrapLock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BootstrapLock b WHERE b.lockName = 'BOOTSTRAP'")
    Optional<BootstrapLock> lockBootstrap();
}



