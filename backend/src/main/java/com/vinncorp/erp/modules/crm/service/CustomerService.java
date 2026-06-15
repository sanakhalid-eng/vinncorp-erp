package com.vinncorp.erp.modules.crm.service;

import com.vinncorp.erp.modules.crm.entity.Contact;
import com.vinncorp.erp.modules.crm.entity.Customer;
import java.util.List;

public interface CustomerService {
    Customer create(Customer customer, Long workspaceId, String actorEmail);
    Customer update(Long id, Customer customer, Long workspaceId, String actorEmail);
    Customer get(Long id, Long workspaceId);
    List<Customer> list(Long workspaceId);
    void delete(Long id, Long workspaceId, String actorEmail);
    void addContact(Long customerId, Long contactId, boolean isPrimary, Long workspaceId);
    void removeContact(Long customerId, Long contactId, Long workspaceId);
    List<Contact> getContacts(Long customerId);
    List<Customer> search(Long workspaceId, String query);
}
