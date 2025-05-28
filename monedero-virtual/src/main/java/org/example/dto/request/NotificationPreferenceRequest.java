package org.example.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.example.model.NotificationChannel;
import org.example.model.NotificationType;

@Data
public class NotificationPreferenceRequest {
    
    private NotificationType notificationType;
    
    private boolean enabled = true;
    
    private NotificationChannel deliveryChannel;
    
    @Min(0)
    @Max(23)
    private Integer quietHoursStart;
    
    @Min(0)
    @Max(23)
    private Integer quietHoursEnd;
}
