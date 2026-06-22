package com.vinncorp.erp.modules.projects.event;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.modules.projects.entity.Project;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MemberAddedEvent extends ApplicationEvent {
    
    private final Project project;
    private final User newMember;
    private final User addedBy;
    
    public MemberAddedEvent(Object source, Project project, User newMember, User addedBy) {
        super(source);
        this.project = project;
        this.newMember = newMember;
        this.addedBy = addedBy;
    }
}


