package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.request.BulkTaskUpdateRequest;
import com.vinncorp.erp.modules.projects.dto.response.BulkTaskUpdateResponse;

public interface BulkTaskService {

    BulkTaskUpdateResponse bulkUpdate(Long workspaceId, BulkTaskUpdateRequest request, String email);

    BulkTaskUpdateResponse bulkDelete(Long workspaceId, BulkTaskUpdateRequest request, String email);
}



