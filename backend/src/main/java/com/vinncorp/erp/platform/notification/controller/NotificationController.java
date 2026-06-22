package com.vinncorp.erp.platform.notification.controller;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.platform.notification.dto.response.NotificationResponse;
import com.vinncorp.erp.modules.projects.dto.response.PaginatedResponse;
import com.vinncorp.erp.platform.notification.service.NotificationService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.shared.mapper.PaginationMapper;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/notifications") @RequiredArgsConstructor
@Tag(name = "Notifications") 
public class NotificationController {
private final NotificationService notificationService;
private final UserRepository userRepository;
@GetMapping @Operation(summary = "Manage notifications", description = "CRUD operations for notifications") 
public ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponse>>> getNotifications(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "false") boolean unreadOnly) {
User user = getUser(authentication);
PageRequest pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
Page<NotificationResponse> notifications = unreadOnly ? notificationService.getUnreadNotifications(user.getId(), pageable) : notificationService.getNotifications(user.getId(), pageable);
return ResponseEntity.ok(new ApiResponse<>(true, "Notifications fetched", PaginationMapper.toPaginatedResponse(notifications, n -> n)));
} @GetMapping("/filtered") @Operation(summary = "Manage notifications", description = "CRUD operations for notifications") 
public ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponse>>> getFilteredNotifications(Authentication authentication, @RequestParam(required = false) String category, @RequestParam(required = false) String type, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
User user = getUser(authentication);
PageRequest pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
Page<NotificationResponse> notifications = notificationService.getFilteredNotifications(user.getId(), category, type, pageable);
return ResponseEntity.ok(new ApiResponse<>(true, "Filtered notifications fetched", PaginationMapper.toPaginatedResponse(notifications, n -> n)));
} @GetMapping("/unread-by-category") @Operation(summary = "Manage notifications", description = "CRUD operations for notifications") 
public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadByCategory(Authentication authentication) {
User user = getUser(authentication);
Map<String, Long> counts = notificationService.getUnreadCountByCategory(user.getId());
return ResponseEntity.ok(new ApiResponse<>(true, "Unread counts fetched", counts));
} @PatchMapping("/{id} /read") @Operation(summary = "Manage notifications", description = "CRUD operations for notifications") 
public ResponseEntity<ApiResponse<Map<String, Boolean>>> markAsRead(
@PathVariable Long id, Authentication authentication) {
User user = getUser(authentication);
boolean updated = notificationService.markAsRead(id, user.getId());
return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read", Map.of("marked", updated)));
} @PatchMapping("/read-all") @Operation(summary = "Manage notifications", description = "CRUD operations for notifications") 
public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(Authentication authentication) {
User user = getUser(authentication);
int count = notificationService.markAllAsRead(user.getId());
return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read", Map.of("markedCount", count)));
} @DeleteMapping("/{id} ") @Operation(summary = "Manage notifications", description = "CRUD operations for notifications") 
public ResponseEntity<ApiResponse<Void>> deleteNotification(
@PathVariable Long id, Authentication authentication) {
User user = getUser(authentication);
notificationService.deleteNotification(id, user.getId());
return ResponseEntity.ok(new ApiResponse<>(true, "Notification deleted", null));
} @GetMapping("/unread-count") @Operation(summary = "Manage notifications", description = "CRUD operations for notifications") 
public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
User user = getUser(authentication);
long count = notificationService.getUnreadCount(user.getId());
return ResponseEntity.ok(new ApiResponse<>(true, "Unread count fetched", count));
} private User getUser(Authentication authentication) {
return userRepository.findByEmail(authentication.getName()) .orElseThrow(() -> new ResourceNotFoundException("User not found"));
}} 