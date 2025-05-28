package org.example.repository;

import org.example.model.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {
    
    /**
     * Busca una categoría por su nombre
     * @param name Nombre de la categoría
     * @return Categoría encontrada
     */
    Optional<TransactionCategory> findByName(String name);
    
    /**
     * Verifica si existe una categoría con el nombre especificado
     * @param name Nombre de la categoría
     * @return true si existe, false en caso contrario
     */
    boolean existsByName(String name);
    
    /**
     * Busca todas las categorías principales (sin categoría padre)
     * @return Lista de categorías principales
     */
    List<TransactionCategory> findByParentCategoryIsNull();
    
    /**
     * Busca todas las subcategorías de una categoría padre
     * @param parentId ID de la categoría padre
     * @return Lista de subcategorías
     */
    List<TransactionCategory> findByParentCategoryId(Long parentId);
    
    /**
     * Busca todas las categorías de tipo gasto
     * @return Lista de categorías de gasto
     */
    List<TransactionCategory> findByExpenseTrue();
    
    /**
     * Busca todas las categorías de tipo ingreso
     * @return Lista de categorías de ingreso
     */
    List<TransactionCategory> findByExpenseFalse();
    
    /**
     * Busca todas las categorías del sistema
     * @return Lista de categorías del sistema
     */
    List<TransactionCategory> findBySystemTrue();
    
    /**
     * Busca todas las categorías personalizadas (no del sistema)
     * @return Lista de categorías personalizadas
     */
    List<TransactionCategory> findBySystemFalse();
    
    /**
     * Cuenta cuántas transacciones están asociadas a una categoría
     * @param categoryId ID de la categoría
     * @return Número de transacciones
     */
    @Query("SELECT COUNT(m) FROM TransactionCategoryMapping m WHERE m.category.id = :categoryId")
    long countTransactionsByCategoryId(Long categoryId);
}
