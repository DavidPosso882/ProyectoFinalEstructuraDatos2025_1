package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa una categoría de recompensas.
 * Las categorías permiten agrupar recompensas similares.
 */
@Entity
@Table(name = "reward_categories")
@Data
@NoArgsConstructor
public class RewardCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column
    private String description;
    
    @Column(name = "icon_name")
    private String iconName;
    
    @Column(name = "color_code")
    private String colorCode;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Reward> rewards = new ArrayList<>();
    
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
     * Añade una recompensa a esta categoría
     */
    public void addReward(Reward reward) {
        rewards.add(reward);
        reward.setCategory(this);
    }
    
    /**
     * Elimina una recompensa de esta categoría
     */
    public void removeReward(Reward reward) {
        rewards.remove(reward);
        reward.setCategory(null);
    }
}
