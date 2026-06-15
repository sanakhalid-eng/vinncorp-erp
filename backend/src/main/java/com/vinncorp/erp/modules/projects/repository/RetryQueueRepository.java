package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.RetryQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RetryQueueRepository extends JpaRepository<RetryQueue, Long> {
    List<RetryQueue> findByStatusAndNextRetryAtLessThanEqual(String status, LocalDateTime time);
    List<RetryQueue> findByTypeAndStatus(String type, String status);
    Page<RetryQueue> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
    long countByTypeAndStatus(String type, String status);
}



