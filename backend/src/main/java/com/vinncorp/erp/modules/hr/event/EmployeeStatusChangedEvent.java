package com.vinncorp.erp.modules.hr.event;

import com.vinncorp.erp.modules.hr.enums.EmployeeStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EmployeeStatusChangedEvent extends ApplicationEvent {

    private final Long employeeId;
    private final EmployeeStatus oldStatus;
    private final EmployeeStatus newStatus;
    private final Long workspaceId;

    public EmployeeStatusChangedEvent(Object source, Long employeeId, EmployeeStatus oldStatus,
                                       EmployeeStatus newStatus, Long workspaceId) {
        super(source);
        this.employeeId = employeeId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.workspaceId = workspaceId;
    }
}
