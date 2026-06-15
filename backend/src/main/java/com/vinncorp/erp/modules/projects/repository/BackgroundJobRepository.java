package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.BackgroundJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface BackgroundJobRepository extends JpaRepository<BackgroundJob, Long> {
    List<BackgroundJob> findByStatusAndNextRetryAtBefore(String status, LocalDateTime time);
    List<BackgroundJob> findByDeadLetterTrue();
    List<BackgroundJob> findByWorkspaceIdAndStatus(Long workspaceId, String status);
    long countByStatus(String status);
}



