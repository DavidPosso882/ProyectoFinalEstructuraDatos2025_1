package org.example.dto.response;

import lombok.Data;
import org.example.model.Notification.RelatedEntityType;
import org.example.model.NotificationType;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private boolean read;
    private Long relatedEntityId;
    private RelatedEntityType relatedEntityType;
}
