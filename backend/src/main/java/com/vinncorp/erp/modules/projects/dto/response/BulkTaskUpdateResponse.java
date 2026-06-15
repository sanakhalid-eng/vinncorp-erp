package com.vinncorp.erp.modules.projects.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkTaskUpdateResponse {
    private int updatedCount;
    private List<Long> failedTaskIds;
}



