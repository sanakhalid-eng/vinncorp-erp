package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ScheduledJob;
import com.vinncorp.erp.modules.projects.entity.ScheduledJobExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledJobExecutionRepository extends JpaRepository<ScheduledJobExecution, Long> {
    Page<ScheduledJobExecution> findByJobOrderByStartedAtDesc(ScheduledJob job, Pageable pageable);
    List<ScheduledJobExecution> findTop10ByOrderByStartedAtDesc();
    List<ScheduledJobExecution> findTop10ByStatusOrderByStartedAtDesc(String status);
    long countByStatusAndJob(String status, ScheduledJob job);
}



