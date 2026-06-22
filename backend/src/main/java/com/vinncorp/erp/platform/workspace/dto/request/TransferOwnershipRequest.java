package com.vinncorp.erp.platform.workspace.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferOwnershipRequest {
    @NotNull
    private Long targetUserId;
}

