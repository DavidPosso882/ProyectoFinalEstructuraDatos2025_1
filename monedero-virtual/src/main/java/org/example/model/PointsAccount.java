package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "points_accounts")
@Data
@NoArgsConstructor
public class PointsAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(name = "available_points", nullable = false)
    private Integer availablePoints = 0;

    @Column(name = "redeemed_points", nullable = false)
    private Integer redeemedPoints = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "pointsAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointsTransaction> pointsTransactions = new ArrayList<>();

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
     * Añade puntos a la cuenta
     * @param points Cantidad de puntos a añadir
     * @param description Descripción de la transacción
     * @param transactionId ID de la transacción relacionada (opcional)
     */
    public void addPoints(int points, String description, Long transactionId) {
        if (points <= 0) {
            return;
        }

        this.totalPoints += points;
        this.availablePoints += points;

        // Crear transacción de puntos
        PointsTransaction pointsTransaction = new PointsTransaction();
        pointsTransaction.setPointsAccount(this);
        pointsTransaction.setAmount(points);
        pointsTransaction.setType(PointsTransactionType.EARNED);
        pointsTransaction.setDescription(description);
        pointsTransaction.setRelatedTransactionId(transactionId);
        
        this.pointsTransactions.add(pointsTransaction);
        
        // Actualizar rango del usuario si es necesario
        updateUserRank();
    }

    /**
     * Canjea puntos de la cuenta
     * @param points Cantidad de puntos a canjear
     * @param description Descripción del canje
     * @return true si se pudieron canjear los puntos, false en caso contrario
     */
    public boolean redeemPoints(int points, String description) {
        if (points <= 0 || points > this.availablePoints) {
            return false;
        }

        this.availablePoints -= points;
        this.redeemedPoints += points;

        // Crear transacción de puntos
        PointsTransaction pointsTransaction = new PointsTransaction();
        pointsTransaction.setPointsAccount(this);
        pointsTransaction.setAmount(points);
        pointsTransaction.setType(PointsTransactionType.REDEEMED);
        pointsTransaction.setDescription(description);
        
        this.pointsTransactions.add(pointsTransaction);
        
        return true;
    }

    /**
     * Actualiza el rango del usuario basado en los puntos totales
     */
    private void updateUserRank() {
        if (user != null) {
            UserRank newRank = UserRank.getRankByPoints(this.totalPoints);
            if (newRank != user.getRank()) {
                user.setRank(newRank);
            }
        }
    }
}
