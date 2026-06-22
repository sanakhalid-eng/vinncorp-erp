package com.vinncorp.erp.modules.finance.repository;

import com.vinncorp.erp.modules.finance.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findAllByInvoiceId(Long invoiceId);

    void deleteAllByInvoiceId(Long invoiceId);
}
