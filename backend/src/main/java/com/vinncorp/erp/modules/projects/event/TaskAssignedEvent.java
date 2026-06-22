package com.vinncorp.erp.modules.projects.event;

import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.platform.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskAssignedEvent extends ApplicationEvent {
    
    private final Task task;
    private final User assignedTo;
    private final User assignedBy;
    
    public TaskAssignedEvent(Object source, Task task, User assignedTo, User assignedBy) {
        super(source);
        this.task = task;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
    }
}


