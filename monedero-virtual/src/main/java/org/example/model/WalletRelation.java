package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa una relación entre dos monederos
 * Esta entidad se utiliza para construir un grafo de relaciones entre monederos
 */
@Entity
@Table(name = "wallet_relations")
@Data
@NoArgsConstructor
public class WalletRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id", nullable = false)
    private Wallet sourceWallet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_wallet_id", nullable = false)
    private Wallet targetWallet;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false)
    private WalletRelationType relationType;
    
    @Column(name = "auto_transfer_percentage")
    private BigDecimal autoTransferPercentage;
    
    @Column(name = "auto_transfer_threshold")
    private BigDecimal autoTransferThreshold;
    
    @Column(name = "auto_transfer_enabled")
    private boolean autoTransferEnabled = false;
    
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
     * Verifica si esta relación permite transferencias automáticas
     * @return true si las transferencias automáticas están habilitadas y configuradas correctamente
     */
    public boolean canAutoTransfer() {
        return autoTransferEnabled && 
               autoTransferPercentage != null && 
               autoTransferPercentage.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Calcula el monto a transferir automáticamente basado en un monto de entrada
     * @param inputAmount Monto de entrada (por ejemplo, un depósito)
     * @return Monto a transferir automáticamente
     */
    public BigDecimal calculateAutoTransferAmount(BigDecimal inputAmount) {
        if (!canAutoTransfer() || inputAmount == null || inputAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Si hay un umbral y el monto es menor, no transferir
        if (autoTransferThreshold != null && inputAmount.compareTo(autoTransferThreshold) < 0) {
            return BigDecimal.ZERO;
        }
        
        // Calcular el porcentaje
        return inputAmount.multiply(autoTransferPercentage.divide(new BigDecimal("100")));
    }
}
