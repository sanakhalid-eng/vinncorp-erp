package com.vinncorp.erp.modules.finance.mapper;

import com.vinncorp.erp.modules.finance.dto.response.PaymentResponse;
import com.vinncorp.erp.modules.finance.entity.Payment;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentMapper {

    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null) return null;
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setInvoiceId(payment.getInvoice() != null ? payment.getInvoice().getId() : null);
        response.setInvoiceNumber(payment.getInvoice() != null ? payment.getInvoice().getInvoiceNumber() : null);
        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        response.setReferenceNumber(payment.getReferenceNumber());
        response.setNotes(payment.getNotes());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }

    public static List<PaymentResponse> toResponseList(List<Payment> payments) {
        if (payments == null) return Collections.emptyList();
        return payments.stream().map(PaymentMapper::toResponse).collect(Collectors.toList());
    }
}
