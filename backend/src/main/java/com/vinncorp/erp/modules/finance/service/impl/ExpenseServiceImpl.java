package com.vinncorp.erp.modules.finance.service.impl;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.finance.dto.request.ExpenseCreateRequest;
import com.vinncorp.erp.modules.finance.dto.request.ExpenseUpdateRequest;
import com.vinncorp.erp.modules.finance.dto.response.ExpenseResponse;
import com.vinncorp.erp.modules.finance.entity.Expense;
import com.vinncorp.erp.modules.finance.enums.ExpenseStatus;
import com.vinncorp.erp.modules.finance.event.ExpenseApprovedEvent;
import com.vinncorp.erp.modules.finance.mapper.ExpenseMapper;
import com.vinncorp.erp.modules.finance.repository.ExpenseRepository;
import com.vinncorp.erp.modules.finance.service.ExpenseService;
import com.vinncorp.erp.modules.finance.specification.ExpenseSpecification;
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
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CurrentWorkspaceResolver workspaceResolver;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAttachmentUrl(request.getAttachmentUrl());
        expense.setStatus(ExpenseStatus.PENDING);
        expense.setWorkspace(workspace);
        expense.setCreatedBy(actor.getId());
        expense.setUpdatedBy(actor.getId());

        Expense saved = expenseRepository.save(expense);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Expense Created", "Expense", saved.getId(), saved.getTitle(),
                null, null, null, null);

        return ExpenseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Expense expense = expenseRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Only pending expenses can be edited");
        }

        String oldValue = expense.getTitle();

        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAttachmentUrl(request.getAttachmentUrl());
        expense.setUpdatedBy(actor.getId());

        Expense saved = expenseRepository.save(expense);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Expense Updated", "Expense", saved.getId(), saved.getTitle(),
                Map.of("title", oldValue), Map.of("title", saved.getTitle()), null, null);

        return ExpenseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Expense expense = expenseRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        expense.softDelete(actor.getId());
        expenseRepository.save(expense);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Expense Deleted", "Expense", id, expense.getTitle(),
                null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Expense expense = expenseRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        return ExpenseMapper.toResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ExpenseResponse> getList(Long workspaceId, String search, ExpenseStatus status,
                                                       String category, LocalDateTime dateFrom, LocalDateTime dateTo,
                                                       Pageable pageable) {
        Specification<Expense> spec = ExpenseSpecification.withFilters(
                workspaceId, search, status, category, dateFrom, dateTo);
        Page<Expense> page = expenseRepository.findAll(spec, pageable);
        return PaginationMapper.toPaginatedResponse(page, ExpenseMapper::toResponse);
    }

    @Override
    @Transactional
    public ExpenseResponse approve(Long id, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Expense expense = expenseRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Only pending expenses can be approved");
        }

        String oldStatus = expense.getStatus().name();
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedBy(actor.getId());
        expense.setApprovedAt(LocalDateTime.now());
        expense.setUpdatedBy(actor.getId());

        Expense saved = expenseRepository.save(expense);

        eventPublisher.publishEvent(new ExpenseApprovedEvent(this, saved.getId(), workspaceId, actorEmail));

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Expense Approved", "Expense", saved.getId(), saved.getTitle(),
                Map.of("status", oldStatus), Map.of("status", saved.getStatus().name()), null, null);

        return ExpenseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse reject(Long id, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Expense expense = expenseRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Only pending expenses can be rejected");
        }

        String oldStatus = expense.getStatus().name();
        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setUpdatedBy(actor.getId());

        Expense saved = expenseRepository.save(expense);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Expense Rejected", "Expense", saved.getId(), saved.getTitle(),
                Map.of("status", oldStatus), Map.of("status", saved.getStatus().name()), null, null);

        return ExpenseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse reimburse(Long id, String actorEmail) {
        Long workspaceId = workspaceResolver.getCurrentWorkspaceId();
        Expense expense = expenseRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (expense.getStatus() != ExpenseStatus.APPROVED) {
            throw new BadRequestException("Only approved expenses can be reimbursed");
        }

        String oldStatus = expense.getStatus().name();
        expense.setStatus(ExpenseStatus.REIMBURSED);
        expense.setUpdatedBy(actor.getId());

        Expense saved = expenseRepository.save(expense);

        auditService.log(workspaceId, actor.getId(), actorEmail,
                "Expense Reimbursed", "Expense", saved.getId(), saved.getTitle(),
                Map.of("status", oldStatus), Map.of("status", saved.getStatus().name()), null, null);

        return ExpenseMapper.toResponse(saved);
    }
}
