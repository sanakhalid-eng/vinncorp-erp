package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.entity.MemberWorkload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Workload balance response")
public class WorkloadBalanceResponse {
    private Long sprintId;
    private String sprintName;
    private List<MemberWorkload> members;
    private int overloadedCount;
    private int underutilizedCount;
    private double giniCoefficient;
}





