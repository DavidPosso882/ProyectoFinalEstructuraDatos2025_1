package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false)
    private WalletType walletType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sourceWallet", cascade = CascadeType.ALL)
    private List<Transaction> outgoingTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "targetWallet", cascade = CascadeType.ALL)
    private List<Transaction> incomingTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduledTransaction> scheduledTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "sourceWallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WalletRelation> outgoingRelations = new HashSet<>();

    @OneToMany(mappedBy = "targetWallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WalletRelation> incomingRelations = new HashSet<>();

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
     * Añade una transacción programada a este monedero
     */
    public void addScheduledTransaction(ScheduledTransaction transaction) {
        scheduledTransactions.add(transaction);
        transaction.setWallet(this);
    }

    /**
     * Elimina una transacción programada de este monedero
     */
    public void removeScheduledTransaction(ScheduledTransaction transaction) {
        scheduledTransactions.remove(transaction);
        transaction.setWallet(null);
    }

    /**
     * Actualiza el saldo del monedero
     * @param amount Cantidad a añadir (positiva) o restar (negativa)
     */
    public void updateBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    /**
     * Verifica si hay saldo suficiente para una operación
     * @param amount Cantidad a verificar
     * @return true si hay saldo suficiente, false en caso contrario
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }

    /**
     * Añade una relación saliente a otro monedero
     * @param targetWallet Monedero destino
     * @param relationType Tipo de relación
     * @return La relación creada
     */
    public WalletRelation addRelationTo(Wallet targetWallet, WalletRelationType relationType) {
        WalletRelation relation = new WalletRelation();
        relation.setSourceWallet(this);
        relation.setTargetWallet(targetWallet);
        relation.setRelationType(relationType);

        outgoingRelations.add(relation);
        targetWallet.getIncomingRelations().add(relation);

        return relation;
    }

    /**
     * Elimina una relación saliente
     * @param relation Relación a eliminar
     */
    public void removeOutgoingRelation(WalletRelation relation) {
        if (relation != null && outgoingRelations.contains(relation)) {
            outgoingRelations.remove(relation);
            relation.getTargetWallet().getIncomingRelations().remove(relation);
            relation.setSourceWallet(null);
            relation.setTargetWallet(null);
        }
    }

    /**
     * Verifica si este monedero tiene una relación directa con otro monedero
     * @param targetWallet Monedero destino
     * @return true si existe una relación directa, false en caso contrario
     */
    public boolean hasDirectRelationWith(Wallet targetWallet) {
        return outgoingRelations.stream()
                .anyMatch(relation -> relation.getTargetWallet().equals(targetWallet));
    }

    /**
     * Obtiene la relación directa con otro monedero, si existe
     * @param targetWallet Monedero destino
     * @return La relación o null si no existe
     */
    public WalletRelation getRelationWith(Wallet targetWallet) {
        return outgoingRelations.stream()
                .filter(relation -> relation.getTargetWallet().equals(targetWallet))
                .findFirst()
                .orElse(null);
    }
}
