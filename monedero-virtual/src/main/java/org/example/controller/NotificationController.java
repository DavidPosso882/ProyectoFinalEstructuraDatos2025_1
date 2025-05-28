package org.example.controller;

import org.example.dto.response.NotificationResponse;
import org.example.security.UserDetailsImpl;
import org.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userDetails.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(userDetails.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Long> getUnreadNotificationsCount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        long count = notificationService.countUnreadNotifications(userDetails.getId());
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NotificationResponse notification = notificationService.markNotificationAsRead(id, userDetails.getId());
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> markAllNotificationsAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.markAllNotificationsAsRead(userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.deleteNotification(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
