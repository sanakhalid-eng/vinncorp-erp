package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuickActionResponse {
    private String key;
    private String label;
    private String method;
    private String path;
    private String icon;
}



