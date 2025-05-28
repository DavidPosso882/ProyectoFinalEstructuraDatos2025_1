package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo que representa una moneda en el sistema.
 */
@Entity
@Table(name = "currencies")
@Data
@NoArgsConstructor
public class Currency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 3)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(name = "exchange_rate", nullable = false)
    private BigDecimal exchangeRate;
    
    @Column(name = "is_base_currency")
    private boolean baseCurrency = false;
    
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
}
