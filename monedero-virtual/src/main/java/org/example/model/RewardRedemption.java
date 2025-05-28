package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Modelo que representa un canje de recompensa por puntos.
 */
@Entity
@Table(name = "reward_redemptions")
@Data
@NoArgsConstructor
public class RewardRedemption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;
    
    @Column(name = "points_spent", nullable = false)
    private Integer pointsSpent;
    
    @Column(name = "redemption_date", nullable = false)
    private LocalDateTime redemptionDate;
    
    @Column(name = "redemption_code")
    private String redemptionCode;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RedemptionStatus status = RedemptionStatus.PENDING;
    
    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;
    
    @Column(name = "notes")
    private String notes;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "points_transaction_id")
    private PointsTransaction pointsTransaction;
    
    @PrePersist
    protected void onCreate() {
        redemptionDate = LocalDateTime.now();
        
        // Generar código de canje único
        if (redemptionCode == null) {
            redemptionCode = generateRedemptionCode();
        }
    }
    
    /**
     * Genera un código de canje único
     */
    private String generateRedemptionCode() {
        // Formato: RDM-[timestamp]-[random]
        return "RDM-" + System.currentTimeMillis() + "-" + 
                String.format("%04d", (int)(Math.random() * 10000));
    }
    
    /**
     * Marca el canje como completado
     */
    public void markAsCompleted() {
        this.status = RedemptionStatus.COMPLETED;
        this.deliveryDate = LocalDateTime.now();
    }
    
    /**
     * Marca el canje como cancelado
     */
    public void markAsCancelled(String reason) {
        this.status = RedemptionStatus.CANCELLED;
        this.notes = reason;
    }
    
    /**
     * Estados posibles para un canje de recompensa
     */
    public enum RedemptionStatus {
        PENDING("Pendiente"),
        PROCESSING("En proceso"),
        COMPLETED("Completado"),
        CANCELLED("Cancelado");
        
        private final String description;
        
        RedemptionStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
