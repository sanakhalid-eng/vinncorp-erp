package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.dto.response.SmartAssignmentResponse;

public interface SmartAssignmentService {

    SmartAssignmentResponse autoAssign(Long taskId, String email);
}



