package com.vinncorp.erp.modules.crm.repository;

import com.vinncorp.erp.modules.crm.entity.CustomerContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerContactRepository extends JpaRepository<CustomerContact, Long> {
    List<CustomerContact> findByCustomerId(Long customerId);
    List<CustomerContact> findByContactId(Long contactId);
    void deleteByCustomerIdAndContactId(Long customerId, Long contactId);
    boolean existsByCustomerIdAndContactId(Long customerId, Long contactId);
}
