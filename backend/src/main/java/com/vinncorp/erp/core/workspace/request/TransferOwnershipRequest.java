package com.vinncorp.erp.core.workspace.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferOwnershipRequest {
    @NotNull
    private Long targetUserId;
}

