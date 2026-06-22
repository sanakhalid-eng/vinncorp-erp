package com.vinncorp.erp.modules.finance.mapper;

import com.vinncorp.erp.modules.finance.dto.response.InvoiceItemResponse;
import com.vinncorp.erp.modules.finance.dto.response.InvoiceResponse;
import com.vinncorp.erp.modules.finance.dto.response.PaymentResponse;
import com.vinncorp.erp.modules.finance.entity.Invoice;
import com.vinncorp.erp.modules.finance.entity.InvoiceItem;
import com.vinncorp.erp.modules.finance.entity.Payment;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceMapper {

    public static InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) return null;
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setWorkspaceId(invoice.getWorkspaceId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setCustomerId(invoice.getCustomerId());
        response.setProjectId(invoice.getProjectId());
        response.setOpportunityId(invoice.getOpportunityId());
        response.setIssueDate(invoice.getIssueDate());
        response.setDueDate(invoice.getDueDate());
        response.setSubtotal(invoice.getSubtotal());
        response.setDiscountAmount(invoice.getDiscountAmount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setAmountPaid(invoice.getAmountPaid());
        response.setBalanceDue(invoice.getBalanceDue());
        response.setNotes(invoice.getNotes());
        response.setStatus(invoice.getStatus() != null ? invoice.getStatus().name() : null);
        response.setSentAt(invoice.getSentAt());
        response.setPaidAt(invoice.getPaidAt());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setUpdatedAt(invoice.getUpdatedAt());
        if (invoice.getItems() != null) {
            response.setItems(invoice.getItems().stream()
                    .map(InvoiceMapper::toItemResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setItems(Collections.emptyList());
        }
        if (invoice.getPayments() != null) {
            response.setPayments(invoice.getPayments().stream()
                    .map(InvoiceMapper::toPaymentResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setPayments(Collections.emptyList());
        }
        return response;
    }

    public static InvoiceItemResponse toItemResponse(InvoiceItem item) {
        if (item == null) return null;
        InvoiceItemResponse response = new InvoiceItemResponse();
        response.setId(item.getId());
        response.setDescription(item.getDescription());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());
        return response;
    }

    public static PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) return null;
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setInvoiceId(payment.getInvoice() != null ? payment.getInvoice().getId() : null);
        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        response.setReferenceNumber(payment.getReferenceNumber());
        response.setNotes(payment.getNotes());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }

    public static List<InvoiceResponse> toResponseList(List<Invoice> invoices) {
        if (invoices == null) return Collections.emptyList();
        return invoices.stream().map(InvoiceMapper::toResponse).collect(Collectors.toList());
    }
}
