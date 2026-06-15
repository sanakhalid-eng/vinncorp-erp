package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.modules.projects.entity.TimesheetApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetApprovalRepository extends JpaRepository<TimesheetApproval, Long> {
    Optional<TimesheetApproval> findByUserAndWeekStart(User user, LocalDate weekStart);
    List<TimesheetApproval> findByUserAndStatus(User user, TimesheetApproval.ApprovalStatus status);
    List<TimesheetApproval> findByStatus(TimesheetApproval.ApprovalStatus status);
    boolean existsByUserAndWeekStartAndStatus(User user, LocalDate weekStart, TimesheetApproval.ApprovalStatus status);
}



