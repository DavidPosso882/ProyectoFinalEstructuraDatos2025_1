package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa una recompensa que puede ser canjeada por puntos.
 */
@Entity
@Table(name = "rewards")
@Data
@NoArgsConstructor
public class Reward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "points_cost", nullable = false)
    private Integer pointsCost;
    
    @Column(name = "monetary_value")
    private BigDecimal monetaryValue;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Column(name = "is_featured")
    private boolean featured = false;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(name = "max_per_user")
    private Integer maxPerUser;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private RewardCategory category;
    
    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL)
    private List<RewardRedemption> redemptions = new ArrayList<>();
    
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
     * Verifica si la recompensa está disponible para canje
     */
    public boolean isAvailable() {
        if (!active) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Verificar fechas de inicio y fin
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }
        
        // Verificar stock
        if (stockQuantity != null && stockQuantity <= 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Reduce el stock de la recompensa en una unidad
     */
    public void reduceStock() {
        if (stockQuantity != null && stockQuantity > 0) {
            stockQuantity--;
        }
    }
    
    /**
     * Añade un canje a esta recompensa
     */
    public void addRedemption(RewardRedemption redemption) {
        redemptions.add(redemption);
        redemption.setReward(this);
    }
}
