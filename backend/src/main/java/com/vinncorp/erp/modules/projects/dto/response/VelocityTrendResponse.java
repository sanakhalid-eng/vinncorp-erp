package com.vinncorp.erp.modules.projects.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Velocity trend response")
public class VelocityTrendResponse {
    private String trend;
    private Double changePercentage;
    private Double averageVelocity;
    private Double lastFiveAverage;
    private VelocityResponse bestSprint;
    private VelocityResponse worstSprint;
    private List<VelocityResponse> history;
}



