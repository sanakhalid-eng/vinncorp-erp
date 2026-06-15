package com.vinncorp.erp.modules.projects.dto.request;

import lombok.Data;

@Data
public class NotificationPreferenceRequest {
    private Boolean taskAssigned;
    private Boolean taskUnassigned;
    private Boolean taskStatusChanged;
    private Boolean taskCreated;
    private Boolean commentMentioned;
    private Boolean commentCreated;
    private Boolean fileUploaded;
    private Boolean dueDateReminder;
    private Boolean emailNotifications;
}



