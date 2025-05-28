package org.example.dto.response;

import lombok.Data;
import org.example.model.NotificationChannel;
import org.example.model.NotificationType;

import java.time.LocalDateTime;

@Data
public class NotificationPreferenceResponse {
    
    private Long id;
    private Long userId;
    private NotificationType notificationType;
    private String notificationTypeDescription;
    private boolean enabled;
    private NotificationChannel deliveryChannel;
    private String deliveryChannelName;
    private Integer quietHoursStart;
    private Integer quietHoursEnd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
