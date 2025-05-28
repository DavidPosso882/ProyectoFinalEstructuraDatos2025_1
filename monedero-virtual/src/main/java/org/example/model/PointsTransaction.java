package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
@Data
@NoArgsConstructor
public class PointsTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private PointsTransactionType type;

    @Column
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "points_account_id", nullable = false)
    private PointsAccount pointsAccount;

    @Column(name = "related_transaction_id")
    private Long relatedTransactionId;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }
}
