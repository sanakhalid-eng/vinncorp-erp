package com.vinncorp.erp.modules.finance.mapper;

import com.vinncorp.erp.modules.finance.dto.response.ExpenseResponse;
import com.vinncorp.erp.modules.finance.entity.Expense;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseMapper {

    public static ExpenseResponse toResponse(Expense expense) {
        if (expense == null) return null;
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setWorkspaceId(expense.getWorkspaceId());
        response.setTitle(expense.getTitle());
        response.setDescription(expense.getDescription());
        response.setCategory(expense.getCategory());
        response.setAmount(expense.getAmount());
        response.setExpenseDate(expense.getExpenseDate());
        response.setAttachmentUrl(expense.getAttachmentUrl());
        response.setStatus(expense.getStatus() != null ? expense.getStatus().name() : null);
        response.setApprovedBy(expense.getApprovedBy());
        response.setApprovedAt(expense.getApprovedAt());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());
        return response;
    }

    public static List<ExpenseResponse> toResponseList(List<Expense> expenses) {
        if (expenses == null) return Collections.emptyList();
        return expenses.stream().map(ExpenseMapper::toResponse).collect(Collectors.toList());
    }
}
