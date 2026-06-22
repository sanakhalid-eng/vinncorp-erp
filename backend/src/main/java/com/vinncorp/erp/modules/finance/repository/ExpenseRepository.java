package com.vinncorp.erp.modules.finance.repository;

import com.vinncorp.erp.modules.finance.entity.Expense;
import com.vinncorp.erp.modules.finance.enums.ExpenseStatus;
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
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndWorkspaceId(Long id, Long workspaceId);

    Page<Expense> findAllByWorkspaceId(Long workspaceId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.workspaceId = :workspaceId AND e.status IN ('APPROVED', 'REIMBURSED')")
    BigDecimal totalExpensesByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
