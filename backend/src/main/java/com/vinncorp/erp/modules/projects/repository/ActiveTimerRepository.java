package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.modules.projects.entity.ActiveTimer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActiveTimerRepository extends JpaRepository<ActiveTimer, Long> {
    Optional<ActiveTimer> findByUser(User user);
    boolean existsByUser(User user);
    void deleteByUser(User user);
}



