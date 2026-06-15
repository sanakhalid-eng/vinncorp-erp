package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SavedSearchRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String queryText;
    private String filtersJson;
    private boolean isDefault;
}


