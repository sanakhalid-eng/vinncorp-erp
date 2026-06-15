package com.vinncorp.erp.modules.projects.event;

import com.vinncorp.erp.modules.projects.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationListener {

    @Autowired
    private EmailService emailService;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    // Task Assigned Notification
    @Async
    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        String taskUrl = baseUrl + "/projects/" + event.getTask().getProject().getId() + "/tasks/" + event.getTask().getId();
        
        String subject = "Task Assigned: " + event.getTask().getTitle();
        String body = "<h2>New Task Assigned</h2>" +
                "<p>Hi " + event.getAssignedTo().getName() + ",</p>" +
                "<p>You have been assigned a new task by " + event.getAssignedBy().getName() + ".</p>" +
                "<div style='background:#f3f4f6; padding:15px; border-radius:8px; margin:15px 0;'>" +
                "<p><strong>Task:</strong> " + event.getTask().getTitle() + "</p>" +
                "<p><strong>Project:</strong> " + event.getTask().getProject().getName() + "</p>" +
                "<p><strong>Due Date:</strong> " + event.getTask().getDueDate() + "</p>" +
                "</div>" +
                "<a href='" + taskUrl + "' style='background:#4f46e5; color:white; padding:10px 20px; text-decoration:none; border-radius:5px; display:inline-block;'>View Task</a>";
        
        emailService.sendSimpleEmail(event.getAssignedTo().getEmail(), subject, body);
    }

    // Task Deadline Reminder
    @Async
    @EventListener
    public void handleTaskDeadlineReminder(TaskDeadlineReminderEvent event) {
        String taskUrl = baseUrl + "/projects/" + event.getTask().getProject().getId() + "/tasks/" + event.getTask().getId();
        
        String subject = "Deadline Reminder: " + event.getTask().getTitle();
        String body = "<h2>⏰ Deadline Reminder</h2>" +
                "<p>Hi " + event.getUser().getName() + ",</p>" +
                "<p>This is a reminder that the following task is due soon:</p>" +
                "<div style='background:#fef3c7; padding:15px; border-radius:8px; margin:15px 0; border-left:4px solid #f59e0b;'>" +
                "<p><strong>Task:</strong> " + event.getTask().getTitle() + "</p>" +
                "<p><strong>Project:</strong> " + event.getTask().getProject().getName() + "</p>" +
                "<p><strong>Due Date:</strong> " + event.getTask().getDueDate() + "</p>" +
                "</div>" +
                "<a href='" + taskUrl + "' style='background:#f59e0b; color:white; padding:10px 20px; text-decoration:none; border-radius:5px; display:inline-block;'>View Task</a>";
        
        emailService.sendSimpleEmail(event.getUser().getEmail(), subject, body);
    }

    // Project Invitation
    @Async
    @EventListener
    public void handleProjectInvitation(ProjectInvitationEvent event) {
        String inviteUrl = baseUrl + "/accept-invitation?token=" + event.getInviteToken();
        
        String subject = "Invitation to Join Project: " + event.getProject().getName();
        String body = "<h2>Project Invitation</h2>" +
                "<p>Hi " + event.getInvitedUser().getName() + ",</p>" +
                "<p>" + event.getInvitedBy().getName() + " has invited you to join the project <strong>" + event.getProject().getName() + "</strong>.</p>" +
                "<div style='background:#eef2ff; padding:15px; border-radius:8px; margin:15px 0;'>" +
                "<p><strong>Project:</strong> " + event.getProject().getName() + "</p>" +
                "<p><strong>Invited By:</strong> " + event.getInvitedBy().getName() + "</p>" +
                "</div>" +
                "<a href='" + inviteUrl + "' style='background:#4f46e5; color:white; padding:10px 20px; text-decoration:none; border-radius:5px; display:inline-block;'>Accept Invitation</a>" +
                "<p style='color:#6b7280; font-size:12px; margin-top:15px;'>This invitation will expire in 7 days.</p>";
        
        emailService.sendSimpleEmail(event.getInvitedUser().getEmail(), subject, body);
    }

    // Member Added Notification
    @Async
    @EventListener
    public void handleMemberAdded(MemberAddedEvent event) {
        String projectUrl = baseUrl + "/projects/" + event.getProject().getId();
        
        String subject = "Added to Project: " + event.getProject().getName();
        String body = "<h2>Welcome to the Project!</h2>" +
                "<p>Hi " + event.getNewMember().getName() + ",</p>" +
                "<p>" + event.getAddedBy().getName() + " has added you to the project <strong>" + event.getProject().getName() + "</strong>.</p>" +
                "<div style='background:#ecfdf5; padding:15px; border-radius:8px; margin:15px 0; border-left:4px solid #10b981;'>" +
                "<p><strong>Project:</strong> " + event.getProject().getName() + "</p>" +
                "</div>" +
                "<a href='" + projectUrl + "' style='background:#10b981; color:white; padding:10px 20px; text-decoration:none; border-radius:5px; display:inline-block;'>Go to Project</a>";
        
        emailService.sendSimpleEmail(event.getNewMember().getEmail(), subject, body);
    }
}



