package org.example.repository;

import org.example.model.PointsAccount;
import org.example.model.PointsTransaction;
import org.example.model.PointsTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
    List<PointsTransaction> findByPointsAccount(PointsAccount pointsAccount);

    Page<PointsTransaction> findByPointsAccountOrderByTransactionDateDesc(PointsAccount pointsAccount, Pageable pageable);

    // Nuevo método para obtener solo transacciones de canje (REDEEMED) paginadas
    Page<PointsTransaction> findByPointsAccountAndTypeOrderByTransactionDateDesc(PointsAccount pointsAccount, PointsTransactionType type, Pageable pageable);

    List<PointsTransaction> findByType(PointsTransactionType type);

    List<PointsTransaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT pt FROM PointsTransaction pt WHERE pt.pointsAccount.user.id = :userId AND pt.transactionDate BETWEEN :startDate AND :endDate ORDER BY pt.transactionDate DESC")
    List<PointsTransaction> findUserPointsTransactionsInPeriod(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT pt.type, SUM(pt.amount) FROM PointsTransaction pt WHERE pt.pointsAccount.user.id = :userId GROUP BY pt.type")
    List<Object[]> sumPointsByTypeForUser(Long userId);
}
