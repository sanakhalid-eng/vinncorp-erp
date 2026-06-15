package com.vinncorp.erp.modules.projects.event;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.modules.projects.entity.Project;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProjectInvitationEvent extends ApplicationEvent {
    
    private final Project project;
    private final User invitedUser;
    private final User invitedBy;
    private final String inviteToken;
    
    public ProjectInvitationEvent(Object source, Project project, User invitedUser, User invitedBy, String inviteToken) {
        super(source);
        this.project = project;
        this.invitedUser = invitedUser;
        this.invitedBy = invitedBy;
        this.inviteToken = inviteToken;
    }
}


