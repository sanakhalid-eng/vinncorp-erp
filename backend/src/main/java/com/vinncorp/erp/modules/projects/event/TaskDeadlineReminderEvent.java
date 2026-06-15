package com.vinncorp.erp.modules.projects.event;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.modules.projects.entity.Task;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskDeadlineReminderEvent extends ApplicationEvent {
    
    private final Task task;
    private final User user;
    
    public TaskDeadlineReminderEvent(Object source, Task task, User user) {
        super(source);
        this.task = task;
        this.user = user;
    }
}


