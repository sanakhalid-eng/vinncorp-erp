package com.vinncorp.erp.modules.projects.dto.response;

import com.vinncorp.erp.modules.projects.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryPredictabilityResponse {
    private Long projectId;
    private double predictabilityScore;
    private double onTimeDeliveryRate;
    private double avgDelayDays;
    private RiskLevel riskLevel;
}



