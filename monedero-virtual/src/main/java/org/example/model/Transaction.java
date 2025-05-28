package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_wallet_id")
    private Wallet targetWallet;

    @Column(name = "points_earned")
    private Integer pointsEarned = 0;

    @Column(name = "is_reversed")
    private boolean reversed = false;

    @Column(name = "reference_id")
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status = TransactionStatus.COMPLETED;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionCategoryMapping> categoryMappings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
        calculatePointsEarned();
    }

    /**
     * Calcula los puntos ganados por esta transacción según su tipo y monto
     */
    private void calculatePointsEarned() {
        if (type == null || amount == null) {
            return;
        }

        // Convertir a entero para simplificar el cálculo
        int amountInt = amount.intValue();

        switch (type) {
            case DEPOSIT:
                // 1 punto por cada 100 unidades
                pointsEarned = amountInt / 100;
                break;
            case WITHDRAWAL:
                // 2 puntos por cada 100 unidades
                pointsEarned = (amountInt / 100) * 2;
                break;
            case TRANSFER:
                // 3 puntos por cada 100 unidades
                pointsEarned = (amountInt / 100) * 3;
                break;
            default:
                pointsEarned = 0;
        }
    }

    /**
     * Añade una categoría a esta transacción
     */
    public void addCategory(TransactionCategory category) {
        TransactionCategoryMapping mapping = new TransactionCategoryMapping();
        mapping.setTransaction(this);
        mapping.setCategory(category);
        categoryMappings.add(mapping);
    }

    /**
     * Añade una categoría a esta transacción con información adicional
     */
    public void addCategory(TransactionCategory category, String notes, boolean autoCategorized) {
        TransactionCategoryMapping mapping = new TransactionCategoryMapping();
        mapping.setTransaction(this);
        mapping.setCategory(category);
        mapping.setNotes(notes);
        mapping.setAutoCategorized(autoCategorized);
        categoryMappings.add(mapping);
    }

    /**
     * Elimina una categoría de esta transacción
     */
    public void removeCategory(TransactionCategory category) {
        categoryMappings.removeIf(mapping -> mapping.getCategory().equals(category));
    }
}
