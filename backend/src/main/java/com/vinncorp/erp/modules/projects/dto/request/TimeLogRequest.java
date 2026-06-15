package com.vinncorp.erp.modules.projects.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeLogRequest {
    private BigDecimal hours;
    private String description;
    private LocalDate logDate;
    private LocalTime startTime;
    private LocalTime endTime;
}



