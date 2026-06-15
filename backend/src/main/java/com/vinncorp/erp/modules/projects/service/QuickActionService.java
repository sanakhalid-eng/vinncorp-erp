package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.QuickActionResponse;

import java.util.List;

public interface QuickActionService {

    List<QuickActionResponse> listForWorkspace(Long workspaceId);
}



