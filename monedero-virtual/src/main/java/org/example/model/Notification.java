package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_read")
    private boolean read = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_entity_type")
    private RelatedEntityType relatedEntityType;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Marca esta notificación como leída
     */
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Tipos de entidades relacionadas con una notificación
     */
    public enum RelatedEntityType {
        TRANSACTION,
        SCHEDULED_TRANSACTION,
        POINTS_TRANSACTION,
        USER_RANK,
        USER,
        REWARD,
        REWARD_REDEMPTION,
        WALLET,
        WALLET_RELATION
    }
}
