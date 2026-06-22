package com.vinncorp.erp.modules.crm.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.audit.service.AuditService;
import com.vinncorp.erp.modules.crm.entity.Contact;
import com.vinncorp.erp.modules.crm.entity.Customer;
import com.vinncorp.erp.modules.crm.entity.CustomerContact;
import com.vinncorp.erp.modules.crm.repository.ContactRepository;
import com.vinncorp.erp.modules.crm.repository.CustomerContactRepository;
import com.vinncorp.erp.modules.crm.repository.CustomerRepository;
import com.vinncorp.erp.modules.crm.service.CustomerService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerContactRepository customerContactRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public Customer create(Customer customer, Long workspaceId, String actorEmail) {
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new BadRequestException("Customer name is required");
        }
        if (customerRepository.existsByNameAndWorkspaceId(customer.getName(), workspaceId)) {
            throw new ConflictException("Customer name already exists in this workspace");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        customer.setWorkspace(workspace);
        customer.setCreatedBy(actor.getId());
        customer.setUpdatedBy(actor.getId());
        Customer saved = customerRepository.save(customer);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "CREATED", "CUSTOMER", saved.getId(), saved.getName(),
                null, Map.of("name", saved.getName(), "industry", saved.getIndustry()),
                null, null);
        return saved;
    }

    @Override
    @Transactional
    public Customer update(Long id, Customer updated, Long workspaceId, String actorEmail) {
        Customer existing = customerRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getIndustry() != null) existing.setIndustry(updated.getIndustry());
        if (updated.getWebsite() != null) existing.setWebsite(updated.getWebsite());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getAddress() != null) existing.setAddress(updated.getAddress());
        if (updated.getNotes() != null) existing.setNotes(updated.getNotes());
        if (updated.getContactOwnerId() != null) existing.setContactOwnerId(updated.getContactOwnerId());
        existing.setUpdatedBy(actor.getId());
        Customer saved = customerRepository.save(existing);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "UPDATED", "CUSTOMER", saved.getId(), saved.getName(), null, null, null, null);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer get(Long id, Long workspaceId) {
        return customerRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> list(Long workspaceId) {
        return customerRepository.findAllByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId, String actorEmail) {
        Customer customer = customerRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        customer.softDelete(actor.getId());
        customer.setUpdatedBy(actor.getId());
        customerRepository.save(customer);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "DELETED", "CUSTOMER", customer.getId(), customer.getName(), null, null, null, null);
    }

    @Override
    @Transactional
    public void addContact(Long customerId, Long contactId, boolean isPrimary, Long workspaceId) {
        Customer customer = customerRepository.findByIdAndWorkspaceId(customerId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Contact contact = contactRepository.findByIdAndWorkspaceId(contactId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
        if (customerContactRepository.existsByCustomerIdAndContactId(customerId, contactId)) {
            throw new ConflictException("Contact already linked to this customer");
        }
        CustomerContact cc = new CustomerContact();
        cc.setCustomer(customer);
        cc.setContact(contact);
        cc.setPrimary(isPrimary);
        customerContactRepository.save(cc);
    }

    @Override
    @Transactional
    public void removeContact(Long customerId, Long contactId, Long workspaceId) {
        customerContactRepository.deleteByCustomerIdAndContactId(customerId, contactId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> getContacts(Long customerId) {
        return customerContactRepository.findByCustomerId(customerId).stream()
                .map(CustomerContact::getContact)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> search(Long workspaceId, String query) {
        if (query == null || query.isBlank()) return list(workspaceId);
        return customerRepository.search(workspaceId, query);
    }
}
