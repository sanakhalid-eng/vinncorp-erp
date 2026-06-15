package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommandPaletteItemResponse {
    private String key;
    private String label;
    private String category;
    private String targetUrl;
    private String shortcut;
}



