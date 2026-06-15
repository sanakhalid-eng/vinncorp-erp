package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavedSearchResponse {
    private Long id;
    private String name;
    private String queryText;
    private String filtersJson;
    private boolean isDefault;
}



