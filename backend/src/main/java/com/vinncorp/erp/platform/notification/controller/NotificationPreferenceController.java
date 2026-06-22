package com.vinncorp.erp.platform.notification.controller;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.notification.dto.request.NotificationPreferenceRequest;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.platform.notification.dto.response.NotificationPreferenceResponse;
import com.vinncorp.erp.platform.notification.entity.NotificationPreference;
import com.vinncorp.erp.platform.notification.repository.NotificationPreferenceRepository;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/notifications/preferences") @RequiredArgsConstructor
@Tag(name = "Notification Preferences") 
public class NotificationPreferenceController {
private final NotificationPreferenceRepository preferenceRepository;
private final UserRepository userRepository;
@GetMapping @Operation(summary = "Manage preferences", description = "CRUD operations for notification preferences") 
public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(Authentication authentication) {
User user = getUser(authentication);
NotificationPreference preference = preferenceRepository.findByUserId(user.getId()) .orElseGet(() -> createDefaultPreferences(user));
return ResponseEntity.ok(new ApiResponse<>(true, "Preferences fetched", toResponse(preference)));
} @PutMapping @Operation(summary = "Manage preferences", description = "CRUD operations for notification preferences") 
public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(Authentication authentication, @RequestBody NotificationPreferenceRequest request) {
User user = getUser(authentication);
NotificationPreference preference = preferenceRepository.findByUserId(user.getId()) .orElseGet(() -> createDefaultPreferences(user));
if (request.getTaskAssigned() != null) preference.setTaskAssigned(request.getTaskAssigned());
if (request.getTaskUnassigned() != null) preference.setTaskUnassigned(request.getTaskUnassigned());
if (request.getTaskStatusChanged() != null) preference.setTaskStatusChanged(request.getTaskStatusChanged());
if (request.getTaskCreated() != null) preference.setTaskCreated(request.getTaskCreated());
if (request.getCommentMentioned() != null) preference.setCommentMentioned(request.getCommentMentioned());
if (request.getCommentCreated() != null) preference.setCommentCreated(request.getCommentCreated());
if (request.getFileUploaded() != null) preference.setFileUploaded(request.getFileUploaded());
if (request.getDueDateReminder() != null) preference.setDueDateReminder(request.getDueDateReminder());
if (request.getEmailNotifications() != null) preference.setEmailNotifications(request.getEmailNotifications());
NotificationPreference updated = preferenceRepository.save(preference);
return ResponseEntity.ok(new ApiResponse<>(true, "Notification preferences updated", toResponse(updated)));
} private NotificationPreference createDefaultPreferences(User user) {
NotificationPreference preference = new NotificationPreference();
preference.setUser(user);
return preferenceRepository.save(preference);
} private NotificationPreferenceResponse toResponse(NotificationPreference preference) {
NotificationPreferenceResponse response = new NotificationPreferenceResponse();
response.setId(preference.getId());
response.setTaskAssigned(preference.isTaskAssigned());
response.setTaskUnassigned(preference.isTaskUnassigned());
response.setTaskStatusChanged(preference.isTaskStatusChanged());
response.setTaskCreated(preference.isTaskCreated());
response.setCommentMentioned(preference.isCommentMentioned());
response.setCommentCreated(preference.isCommentCreated());
response.setFileUploaded(preference.isFileUploaded());
response.setDueDateReminder(preference.isDueDateReminder());
response.setEmailNotifications(preference.isEmailNotifications());
response.setCreatedAt(preference.getCreatedAt());
response.setUpdatedAt(preference.getUpdatedAt());
return response;
} private User getUser(Authentication authentication) {
return userRepository.findByEmail(authentication.getName()) .orElseThrow(() -> new ResourceNotFoundException("User not found"));
}} 