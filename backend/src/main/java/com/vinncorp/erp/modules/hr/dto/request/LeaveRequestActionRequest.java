package com.vinncorp.erp.modules.hr.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestActionRequest {
    private String rejectionReason;
}
