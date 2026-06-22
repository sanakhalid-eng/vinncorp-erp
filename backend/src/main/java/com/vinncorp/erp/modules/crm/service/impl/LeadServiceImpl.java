package com.vinncorp.erp.modules.crm.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.audit.service.AuditService;
import com.vinncorp.erp.modules.crm.entity.Contact;
import com.vinncorp.erp.modules.crm.entity.Customer;
import com.vinncorp.erp.modules.crm.entity.CustomerContact;
import com.vinncorp.erp.modules.crm.entity.Lead;
import com.vinncorp.erp.modules.crm.entity.Opportunity;
import com.vinncorp.erp.modules.crm.entity.Pipeline;
import com.vinncorp.erp.modules.crm.entity.PipelineStage;
import com.vinncorp.erp.modules.crm.enums.LeadStatus;
import com.vinncorp.erp.modules.crm.event.LeadConvertedEvent;
import com.vinncorp.erp.modules.crm.repository.ContactRepository;
import com.vinncorp.erp.modules.crm.repository.CustomerContactRepository;
import com.vinncorp.erp.modules.crm.repository.CustomerRepository;
import com.vinncorp.erp.modules.crm.repository.LeadRepository;
import com.vinncorp.erp.modules.crm.repository.OpportunityRepository;
import com.vinncorp.erp.modules.crm.repository.PipelineRepository;
import com.vinncorp.erp.modules.crm.repository.PipelineStageRepository;
import com.vinncorp.erp.modules.crm.service.LeadService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final ContactRepository contactRepository;
    private final CustomerContactRepository customerContactRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AuditService auditService;
    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final OpportunityRepository opportunityRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Lead create(Lead lead, Long workspaceId, String actorEmail) {
        if (lead.getFirstName() == null || lead.getFirstName().isBlank()) {
            throw new BadRequestException("First name is required");
        }
        if (lead.getLastName() == null || lead.getLastName().isBlank()) {
            throw new BadRequestException("Last name is required");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        lead.setWorkspace(workspace);
        lead.setOwnerId(actor.getId());
        lead.setCreatedBy(actor.getId());
        lead.setUpdatedBy(actor.getId());
        Lead saved = leadRepository.save(lead);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "CREATED", "LEAD", saved.getId(), saved.getFullName(),
                null, Map.of("company", saved.getCompany(), "source", saved.getSource().name()),
                null, null);
        return saved;
    }

    @Override
    @Transactional
    public Lead update(Long id, Lead updated, Long workspaceId, String actorEmail) {
        Lead existing = leadRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found: " + id));
        if (existing.getStatus() == LeadStatus.CONVERTED) {
            throw new BadRequestException("Cannot edit a converted lead");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getCompany() != null) existing.setCompany(updated.getCompany());
        if (updated.getJobTitle() != null) existing.setJobTitle(updated.getJobTitle());
        if (updated.getSource() != null) existing.setSource(updated.getSource());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getNotes() != null) existing.setNotes(updated.getNotes());
        existing.setUpdatedBy(actor.getId());
        Lead saved = leadRepository.save(existing);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "UPDATED", "LEAD", saved.getId(), saved.getFullName(), null, null, null, null);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Lead get(Long id, Long workspaceId) {
        return leadRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lead> list(Long workspaceId) {
        return leadRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lead> listByStatus(Long workspaceId, LeadStatus status) {
        return leadRepository.findByWorkspaceIdAndStatusOrderByCreatedAtDesc(workspaceId, status);
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId, String actorEmail) {
        Lead lead = leadRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found: " + id));
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new BadRequestException("Cannot delete a converted lead");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        lead.softDelete(actor.getId());
        lead.setUpdatedBy(actor.getId());
        leadRepository.save(lead);
        auditService.log(workspaceId, actor.getId(), actorEmail,
                "DELETED", "LEAD", lead.getId(), lead.getFullName(), null, null, null, null);
    }

    @Override
    @Transactional
    public Lead convert(Long id, Long workspaceId, String actorEmail) {
        Lead lead = leadRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found: " + id));
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new BadRequestException("Lead already converted");
        }
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        // 1. Create Customer
        Customer customer = new Customer();
        customer.setName(lead.getCompany() != null ? lead.getCompany() : lead.getFullName());
        customer.setEmail(lead.getEmail());
        customer.setPhone(lead.getPhone());
        customer.setWorkspace(workspace);
        customer.setContactOwnerId(lead.getOwnerId());
        customer.setCreatedBy(actor.getId());
        customer.setUpdatedBy(actor.getId());
        Customer savedCustomer = customerRepository.save(customer);

        // 2. Create Contact from Lead
        Contact contact = new Contact();
        contact.setFirstName(lead.getFirstName());
        contact.setLastName(lead.getLastName());
        contact.setEmail(lead.getEmail());
        contact.setPhone(lead.getPhone());
        contact.setCompany(lead.getCompany());
        contact.setJobTitle(lead.getJobTitle());
        contact.setWorkspace(workspace);
        contact.setCreatedBy(actor.getId());
        contact.setUpdatedBy(actor.getId());
        Contact savedContact = contactRepository.save(contact);

        // 3. Link Customer-Contact
        CustomerContact cc = new CustomerContact();
        cc.setCustomer(savedCustomer);
        cc.setContact(savedContact);
        cc.setPrimary(true);
        customerContactRepository.save(cc);

        // 4. Create initial Opportunity on default pipeline
        Opportunity savedOpportunity = null;
        Pipeline defaultPipeline = pipelineRepository.findByWorkspaceIdAndIsDefaultTrue(workspaceId)
                .orElse(null);
        if (defaultPipeline == null) {
            List<Pipeline> pipelines = pipelineRepository.findAllByWorkspaceIdOrderByDisplayOrderAsc(workspaceId);
            defaultPipeline = pipelines.isEmpty() ? null : pipelines.get(0);
        }
        if (defaultPipeline != null) {
            PipelineStage newStage = pipelineStageRepository.findByPipelineIdOrderByDisplayOrderAsc(defaultPipeline.getId())
                    .stream()
                    .filter(s -> !s.isWon() && !s.isLost())
                    .findFirst()
                    .orElse(null);
            if (newStage != null) {
                Opportunity opp = new Opportunity();
                opp.setTitle(lead.getFullName() + " - New Deal");
                opp.setWorkspace(workspace);
                opp.setStage(newStage);
                opp.setCustomerId(savedCustomer.getId());
                opp.setLeadId(lead.getId());
                opp.setOwnerId(lead.getOwnerId());
                opp.setProbabilityPct(newStage.getProbabilityPct());
                opp.setCreatedBy(actor.getId());
                opp.setUpdatedBy(actor.getId());
                savedOpportunity = opportunityRepository.save(opp);
            }
        }

        // 5. Mark lead as converted
        lead.setStatus(LeadStatus.CONVERTED);
        lead.setConvertedCustomerId(savedCustomer.getId());
        lead.setConvertedBy(actor.getId());
        lead.setConvertedAt(LocalDateTime.now());
        lead.setUpdatedBy(actor.getId());
        Lead savedLead = leadRepository.save(lead);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "LEAD_CONVERTED", "LEAD", lead.getId(), lead.getFullName(),
                null, Map.of(
                        "customerId", savedCustomer.getId(),
                        "contactId", savedContact.getId(),
                        "opportunityId", savedOpportunity != null ? savedOpportunity.getId() : null
                ),
                null, null);

        // 6. Publish event for notifications
        eventPublisher.publishEvent(new LeadConvertedEvent(
                this, lead.getId(), workspaceId, actor.getId(), actorEmail,
                savedCustomer.getId(), savedContact.getId(),
                savedOpportunity != null ? savedOpportunity.getId() : null
        ));

        return savedLead;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Long workspaceId, LeadStatus status) {
        return leadRepository.countByWorkspaceIdAndStatus(workspaceId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lead> search(Long workspaceId, String query) {
        if (query == null || query.isBlank()) return list(workspaceId);
        return leadRepository.search(workspaceId, query);
    }
}
