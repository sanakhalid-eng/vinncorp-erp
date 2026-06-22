package com.vinncorp.erp.modules.finance.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.finance.dto.request.PaymentCreateRequest;
import com.vinncorp.erp.modules.finance.dto.request.PaymentUpdateRequest;
import com.vinncorp.erp.modules.finance.dto.response.PaymentResponse;
import com.vinncorp.erp.modules.finance.entity.Invoice;
import com.vinncorp.erp.modules.finance.entity.Payment;
import com.vinncorp.erp.modules.finance.enums.PaymentMethod;
import com.vinncorp.erp.modules.finance.event.InvoicePaidEvent;
import com.vinncorp.erp.modules.finance.mapper.PaymentMapper;
import com.vinncorp.erp.modules.finance.repository.InvoiceRepository;
import com.vinncorp.erp.modules.finance.repository.PaymentRepository;
import com.vinncorp.erp.modules.finance.service.PaymentService;
import com.vinncorp.erp.platform.audit.service.AuditService;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.mapper.PaginationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CurrentWorkspaceResolver workspaceResolver;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponse create(PaymentCreateRequest request, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Invoice invoice = invoiceRepository.findByIdAndWorkspaceId(request.getInvoiceId(), workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + request.getInvoiceId()));

        if (invoice.getStatus().name().equals("CANCELLED")) {
            throw new BadRequestException("Cannot add payment to a cancelled invoice");
        }

        if (request.getAmount().compareTo(invoice.getBalanceDue()) > 0) {
            throw new BadRequestException("Payment amount cannot exceed invoice balance due of " + invoice.getBalanceDue());
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid payment method: " + request.getPaymentMethod());
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now());
        payment.setPaymentMethod(method);
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setNotes(request.getNotes());
        payment.setWorkspace(workspace);
        payment.setCreatedBy(actor.getId());
        payment.setUpdatedBy(actor.getId());

        Payment saved = paymentRepository.save(payment);

        updateInvoiceFromPayments(invoice, actor.getId());

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Payment Created", "Payment", saved.getId(), "Payment for Invoice " + invoice.getInvoiceNumber(),
                null, null, null, null);

        return PaymentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse update(Long id, PaymentUpdateRequest request, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Payment payment = paymentRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Invoice invoice = payment.getInvoice();

        BigDecimal oldAmount = payment.getAmount();

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid payment method: " + request.getPaymentMethod());
        }

        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(method);
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setNotes(request.getNotes());
        payment.setUpdatedBy(actor.getId());

        Payment saved = paymentRepository.save(payment);

        updateInvoiceFromPayments(invoice, actor.getId());

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Payment Updated", "Payment", saved.getId(), "Payment for Invoice " + invoice.getInvoiceNumber(),
                Map.of("amount", oldAmount), Map.of("amount", saved.getAmount()), null, null);

        return PaymentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Payment payment = paymentRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Invoice invoice = payment.getInvoice();

        payment.softDelete(actor.getId());
        paymentRepository.save(payment);

        updateInvoiceFromPayments(invoice, actor.getId());

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Payment Deleted", "Payment", id, "Payment for Invoice " + invoice.getInvoiceNumber(),
                null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Payment payment = paymentRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        return PaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<PaymentResponse> getList(Long workspaceId, Pageable pageable) {
        Page<Payment> page = paymentRepository.findAllByWorkspaceId(workspaceId, pageable);
        return PaginationMapper.toPaginatedResponse(page, PaymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByInvoiceId(Long invoiceId) {
        List<Payment> payments = paymentRepository.findAllByInvoiceId(invoiceId);
        return PaymentMapper.toResponseList(payments);
    }

    private void updateInvoiceFromPayments(Invoice invoice, Long actorId) {
        BigDecimal totalPaid = invoice.getPayments().stream()
                .filter(p -> p.getDeletedAt() == null)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setAmountPaid(totalPaid);
        invoice.setBalanceDue(invoice.getTotalAmount().subtract(totalPaid));
        invoice.setUpdatedBy(actorId);

        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0 && totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(com.vinncorp.erp.modules.finance.enums.InvoiceStatus.PAID);
            if (invoice.getPaidAt() == null) {
                invoice.setPaidAt(LocalDateTime.now());
            }
            eventPublisher.publishEvent(new InvoicePaidEvent(this, invoice.getId(),
                    invoice.getWorkspaceId(), null));
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0
                && invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(com.vinncorp.erp.modules.finance.enums.InvoiceStatus.PARTIALLY_PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) == 0
                && invoice.getStatus() == com.vinncorp.erp.modules.finance.enums.InvoiceStatus.PARTIALLY_PAID) {
            invoice.setStatus(com.vinncorp.erp.modules.finance.enums.InvoiceStatus.SENT);
        }

        invoiceRepository.save(invoice);
    }
}
