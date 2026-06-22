package com.vinncorp.erp.modules.finance.repository;

import com.vinncorp.erp.modules.finance.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdAndWorkspaceId(Long id, Long workspaceId);

    Page<Payment> findAllByWorkspaceId(Long workspaceId, Pageable pageable);

    List<Payment> findAllByInvoiceId(Long invoiceId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoiceId = :invoiceId")
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") Long invoiceId);
}
