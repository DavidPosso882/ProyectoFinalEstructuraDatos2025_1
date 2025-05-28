package org.example.repository;

import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionCategoryMappingRepository extends JpaRepository<TransactionCategoryMapping, Long> {
    
    /**
     * Busca todos los mapeos de categorías para una transacción
     * @param transaction Transacción
     * @return Lista de mapeos de categorías
     */
    List<TransactionCategoryMapping> findByTransaction(Transaction transaction);
    
    /**
     * Busca todos los mapeos para una categoría
     * @param category Categoría
     * @return Lista de mapeos de categorías
     */
    List<TransactionCategoryMapping> findByCategory(TransactionCategory category);
    
    /**
     * Busca todos los mapeos para una categoría en un rango de fechas
     * @param category Categoría
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de mapeos de categorías
     */
    @Query("SELECT m FROM TransactionCategoryMapping m WHERE m.category = :category AND m.transaction.transactionDate BETWEEN :startDate AND :endDate")
    List<TransactionCategoryMapping> findByCategoryAndDateRange(TransactionCategory category, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Busca todos los mapeos para un usuario en un rango de fechas
     * @param userId ID del usuario
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de mapeos de categorías
     */
    @Query("SELECT m FROM TransactionCategoryMapping m WHERE (m.transaction.sourceWallet.user.id = :userId OR m.transaction.targetWallet.user.id = :userId) AND m.transaction.transactionDate BETWEEN :startDate AND :endDate")
    List<TransactionCategoryMapping> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Busca todos los mapeos para un monedero en un rango de fechas
     * @param walletId ID del monedero
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de mapeos de categorías
     */
    @Query("SELECT m FROM TransactionCategoryMapping m WHERE (m.transaction.sourceWallet.id = :walletId OR m.transaction.targetWallet.id = :walletId) AND m.transaction.transactionDate BETWEEN :startDate AND :endDate")
    List<TransactionCategoryMapping> findByWalletIdAndDateRange(Long walletId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Elimina todos los mapeos para una transacción
     * @param transaction Transacción
     */
    void deleteByTransaction(Transaction transaction);
    
    /**
     * Elimina todos los mapeos para una categoría
     * @param category Categoría
     */
    void deleteByCategory(TransactionCategory category);
}
