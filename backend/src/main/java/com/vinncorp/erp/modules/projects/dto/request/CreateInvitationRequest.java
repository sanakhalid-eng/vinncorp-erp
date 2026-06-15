package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateInvitationRequest {
    @NotBlank
    @Email
    private String email;

    @NotNull
    private Long roleId;
}



