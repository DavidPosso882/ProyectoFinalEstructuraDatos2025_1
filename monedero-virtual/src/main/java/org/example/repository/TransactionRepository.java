package org.example.repository;

import org.example.model.Transaction;
import org.example.model.TransactionStatus;
import org.example.model.TransactionType;
import org.example.model.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceWallet(Wallet wallet);

    List<Transaction> findByTargetWallet(Wallet wallet);

    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet = :wallet OR t.targetWallet = :wallet ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllByWallet(Wallet wallet, Pageable pageable);

    List<Transaction> findByType(TransactionType type);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceWallet.user.id = :userId OR t.targetWallet.user.id = :userId) AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findUserTransactionsInPeriod(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t.type, COUNT(t) FROM Transaction t WHERE t.sourceWallet.user.id = :userId GROUP BY t.type")
    List<Object[]> countTransactionsByTypeForUser(Long userId);

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.sourceWallet sw " +
           "LEFT JOIN FETCH sw.user " +
           "LEFT JOIN FETCH t.targetWallet tw " +
           "LEFT JOIN FETCH tw.user " +
           "WHERE ((t.type = 'DEPOSIT' AND tw.user.id = :userId) " +
           "OR (t.type <> 'DEPOSIT' AND (sw.user.id = :userId OR tw.user.id = :userId))) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId) AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByWalletIdAndDateRange(Long walletId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Encuentra las transacciones más recientes de un monedero
     * @param walletId ID del monedero
     * @param limit Número máximo de transacciones a devolver
     * @return Lista de transacciones ordenadas por fecha (más recientes primero)
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentByWallet(@Param("walletId") Long walletId, Pageable pageable);

    /**
     * Método de conveniencia para obtener un número específico de transacciones recientes
     */
    default List<Transaction> findRecentByWallet(Long walletId, int limit) {
        return findRecentByWallet(walletId, Pageable.ofSize(limit));
    }

    @Query("SELECT t FROM Transaction t WHERE (t.sourceWallet.user.id = :userId OR t.targetWallet.user.id = :userId) AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findAllUserTransactionsInPeriod(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Obtiene todas las transacciones de un usuario sin duplicados
     * @param userId ID del usuario
     * @param pageable Configuración de paginación
     * @return Página de transacciones únicas del usuario
     */
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "LEFT JOIN FETCH t.sourceWallet sw " +
           "LEFT JOIN FETCH sw.user " +
           "LEFT JOIN FETCH t.targetWallet tw " +
           "LEFT JOIN FETCH tw.user " +
           "WHERE (sw.user.id = :userId OR tw.user.id = :userId) " +
           "ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllUserTransactions(@Param("userId") Long userId, Pageable pageable);
}
