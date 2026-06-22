package com.vinncorp.erp.modules.finance.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.finance.dto.request.InvoiceCreateRequest;
import com.vinncorp.erp.modules.finance.dto.request.InvoiceUpdateRequest;
import com.vinncorp.erp.modules.finance.dto.response.InvoiceResponse;
import com.vinncorp.erp.modules.finance.entity.Invoice;
import com.vinncorp.erp.modules.finance.entity.InvoiceItem;
import com.vinncorp.erp.modules.finance.enums.InvoiceStatus;
import com.vinncorp.erp.modules.finance.event.InvoicePaidEvent;
import com.vinncorp.erp.modules.finance.event.InvoiceSentEvent;
import com.vinncorp.erp.modules.finance.mapper.InvoiceMapper;
import com.vinncorp.erp.modules.finance.repository.InvoiceRepository;
import com.vinncorp.erp.modules.finance.service.InvoiceService;
import com.vinncorp.erp.modules.finance.specification.InvoiceSpecification;
import com.vinncorp.erp.platform.audit.service.AuditService;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.mapper.PaginationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CurrentWorkspaceResolver workspaceResolver;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public InvoiceResponse create(InvoiceCreateRequest request, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (invoiceRepository.existsByInvoiceNumberAndWorkspaceId(request.getInvoiceNumber(), workspaceId)) {
            throw new BadRequestException("Invoice number already exists in this workspace");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setCustomerId(request.getCustomerId());
        invoice.setProjectId(request.getProjectId());
        invoice.setOpportunityId(request.getOpportunityId());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        invoice.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        invoice.setNotes(request.getNotes());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setWorkspace(workspace);
        invoice.setCreatedBy(actor.getId());
        invoice.setUpdatedBy(actor.getId());

        List<InvoiceItem> items = new ArrayList<>();
        for (var itemReq : request.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setDescription(itemReq.getDescription());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setTotalPrice(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            item.setInvoice(invoice);
            items.add(item);
        }
        invoice.setItems(items);

        recalculateTotals(invoice);

        Invoice saved = invoiceRepository.save(invoice);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Invoice Created", "Invoice", saved.getId(), saved.getInvoiceNumber(),
                null, null, null, null);

        return InvoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse update(Long id, InvoiceUpdateRequest request, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Invoice invoice = invoiceRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!invoice.getInvoiceNumber().equals(request.getInvoiceNumber())
                && invoiceRepository.existsByInvoiceNumberAndWorkspaceIdAndIdNot(request.getInvoiceNumber(), workspaceId, id)) {
            throw new BadRequestException("Invoice number already exists in this workspace");
        }

        String oldValue = invoice.getStatus().name();

        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setCustomerId(request.getCustomerId());
        invoice.setProjectId(request.getProjectId());
        invoice.setOpportunityId(request.getOpportunityId());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        invoice.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        invoice.setNotes(request.getNotes());
        invoice.setUpdatedBy(actor.getId());

        if (request.getItems() != null) {
            invoice.getItems().clear();
            List<InvoiceItem> items = new ArrayList<>();
            for (var itemReq : request.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setDescription(itemReq.getDescription());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(itemReq.getUnitPrice());
                item.setTotalPrice(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
                item.setInvoice(invoice);
                items.add(item);
            }
            invoice.setItems(items);
        }

        recalculateTotals(invoice);

        Invoice saved = invoiceRepository.save(invoice);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Invoice Updated", "Invoice", saved.getId(), saved.getInvoiceNumber(),
                Map.of("status", oldValue), Map.of("status", saved.getStatus().name()), null, null);

        return InvoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Invoice invoice = invoiceRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        invoice.softDelete(actor.getId());
        invoiceRepository.save(invoice);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Invoice Deleted", "Invoice", id, invoice.getInvoiceNumber(),
                null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Invoice invoice = invoiceRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        return InvoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InvoiceResponse> getList(Long workspaceId, String search, InvoiceStatus status,
                                                       Long customerId, LocalDateTime dateFrom, LocalDateTime dateTo,
                                                       Pageable pageable) {
        Specification<Invoice> spec = InvoiceSpecification.withFilters(
                workspaceId, search, status, customerId, dateFrom, dateTo);
        Page<Invoice> page = invoiceRepository.findAll(spec, pageable);
        return PaginationMapper.toPaginatedResponse(page, InvoiceMapper::toResponse);
    }

    @Override
    @Transactional
    public InvoiceResponse sendInvoice(Long id, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Invoice invoice = invoiceRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot send a cancelled invoice");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already paid");
        }

        String oldStatus = invoice.getStatus().name();
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setSentAt(LocalDateTime.now());
        invoice.setUpdatedBy(actor.getId());
        autoUpdateOverdueStatus(invoice);

        Invoice saved = invoiceRepository.save(invoice);

        eventPublisher.publishEvent(new InvoiceSentEvent(this, saved.getId(), workspaceId, actorEmail));

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Invoice Sent", "Invoice", saved.getId(), saved.getInvoiceNumber(),
                Map.of("status", oldStatus), Map.of("status", saved.getStatus().name()), null, null);

        return InvoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse markAsPaid(Long id, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Invoice invoice = invoiceRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot mark a cancelled invoice as paid");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already paid");
        }

        String oldStatus = invoice.getStatus().name();
        invoice.setAmountPaid(invoice.getTotalAmount());
        invoice.setBalanceDue(BigDecimal.ZERO);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setUpdatedBy(actor.getId());

        Invoice saved = invoiceRepository.save(invoice);

        eventPublisher.publishEvent(new InvoicePaidEvent(this, saved.getId(), workspaceId, actorEmail));

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Invoice Marked Paid", "Invoice", saved.getId(), saved.getInvoiceNumber(),
                Map.of("status", oldStatus), Map.of("status", saved.getStatus().name()), null, null);

        return InvoiceMapper.toResponse(saved);
    }

    void recalculateTotals(Invoice invoice) {
        BigDecimal subtotal = invoice.getItems().stream()
                .map(InvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal tax = invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO;

        invoice.setSubtotal(subtotal);
        invoice.setTotalAmount(subtotal.subtract(discount).add(tax));
        recalculateBalance(invoice);
    }

    void recalculateBalance(Invoice invoice) {
        BigDecimal paid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        invoice.setBalanceDue(invoice.getTotalAmount().subtract(paid));
    }

    void autoUpdateStatus(Invoice invoice) {
        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0
                && invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            if (invoice.getPaidAt() == null) {
                invoice.setPaidAt(LocalDateTime.now());
            }
        } else if (invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0
                && invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        } else {
            autoUpdateOverdueStatus(invoice);
        }
    }

    void autoUpdateOverdueStatus(Invoice invoice) {
        if (invoice.getStatus() != InvoiceStatus.PAID
                && invoice.getStatus() != InvoiceStatus.CANCELLED
                && invoice.getDueDate() != null
                && invoice.getDueDate().isBefore(LocalDateTime.now())) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
        }
    }
}
