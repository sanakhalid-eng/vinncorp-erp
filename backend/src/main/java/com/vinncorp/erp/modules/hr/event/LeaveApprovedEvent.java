package com.vinncorp.erp.modules.hr.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeaveApprovedEvent extends ApplicationEvent {

    private final Long employeeId;
    private final Long leaveRequestId;
    private final Long workspaceId;

    public LeaveApprovedEvent(Object source, Long employeeId, Long leaveRequestId, Long workspaceId) {
        super(source);
        this.employeeId = employeeId;
        this.leaveRequestId = leaveRequestId;
        this.workspaceId = workspaceId;
    }
}
