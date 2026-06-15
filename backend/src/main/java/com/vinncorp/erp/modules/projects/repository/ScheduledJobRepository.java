package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.ScheduledJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {
    Optional<ScheduledJob> findByJobName(String jobName);
}



