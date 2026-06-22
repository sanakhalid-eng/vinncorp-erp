package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.platform.user.entity.UserSummary;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EditHistoryResponse {

    private Long id;
    private String oldContent;
    private UserSummary editedBy;
    private LocalDateTime editedAt;
}



