package com.vinncorp.erp.modules.finance.repository;

import com.vinncorp.erp.modules.finance.entity.Invoice;
import com.vinncorp.erp.modules.finance.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByIdAndWorkspaceId(Long id, Long workspaceId);

    Page<Invoice> findAllByWorkspaceId(Long workspaceId, Pageable pageable);

    long countByWorkspaceIdAndStatus(Long workspaceId, InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.workspaceId = :workspaceId AND i.status NOT IN ('CANCELLED', 'DRAFT')")
    BigDecimal totalRevenueByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i WHERE i.workspaceId = :workspaceId AND i.status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal totalOutstandingByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.workspaceId = :workspaceId AND i.status IN ('SENT', 'PARTIALLY_PAID') AND i.dueDate < CURRENT_TIMESTAMP")
    long countOverdueByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.workspaceId = :workspaceId AND i.status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')")
    long countOutstandingByWorkspaceId(@Param("workspaceId") Long workspaceId);

    boolean existsByInvoiceNumberAndWorkspaceId(String invoiceNumber, Long workspaceId);

    boolean existsByInvoiceNumberAndWorkspaceIdAndIdNot(String invoiceNumber, Long workspaceId, Long id);
}
