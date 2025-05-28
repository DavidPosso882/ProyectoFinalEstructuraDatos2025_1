package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Preferencias de notificaciones para un usuario
 */
@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;
    
    @Column(name = "enabled")
    private boolean enabled = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel")
    private NotificationChannel deliveryChannel;
    
    @Column(name = "quiet_hours_start")
    private Integer quietHoursStart;
    
    @Column(name = "quiet_hours_end")
    private Integer quietHoursEnd;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica si la notificación debe ser enviada en este momento
     * @return true si debe enviarse, false en caso contrario
     */
    public boolean shouldDeliverNow() {
        if (!enabled) {
            return false;
        }
        
        // Si no hay horas de silencio configuradas, siempre enviar
        if (quietHoursStart == null || quietHoursEnd == null) {
            return true;
        }
        
        // Verificar si estamos en horas de silencio
        int currentHour = LocalDateTime.now().getHour();
        
        if (quietHoursStart < quietHoursEnd) {
            // Período normal (ej: 22:00 - 07:00)
            return currentHour < quietHoursStart || currentHour >= quietHoursEnd;
        } else {
            // Período que cruza la medianoche (ej: 22:00 - 07:00)
            return currentHour < quietHoursStart && currentHour >= quietHoursEnd;
        }
    }
}
