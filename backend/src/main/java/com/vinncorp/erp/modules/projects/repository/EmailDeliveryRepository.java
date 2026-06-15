package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.EmailDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailDeliveryRepository extends JpaRepository<EmailDelivery, Long> {
    Page<EmailDelivery> findByStatus(String status, Pageable pageable);
    List<EmailDelivery> findByStatusAndRetryCountLessThan(String status, int maxRetries);
    long countByStatus(String status);
}



