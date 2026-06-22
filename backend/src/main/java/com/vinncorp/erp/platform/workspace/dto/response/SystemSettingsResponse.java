package com.vinncorp.erp.platform.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class SystemSettingsResponse {
    private boolean workspaceOwner;
    private String workspaceName;
    private Map<String, Boolean> features;
}

