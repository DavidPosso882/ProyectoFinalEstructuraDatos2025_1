package org.example.service;

import org.example.dto.request.TransactionCategoryMappingRequest;
import org.example.dto.request.TransactionCategoryRequest;
import org.example.dto.response.TransactionCategoryMappingResponse;
import org.example.dto.response.TransactionCategoryResponse;

import java.util.List;

public interface TransactionCategoryService {
    
    /**
     * Crea una nueva categoría de transacción
     * @param request Datos de la categoría
     * @return Categoría creada
     */
    TransactionCategoryResponse createCategory(TransactionCategoryRequest request);
    
    /**
     * Actualiza una categoría existente
     * @param id ID de la categoría
     * @param request Datos actualizados
     * @return Categoría actualizada
     */
    TransactionCategoryResponse updateCategory(Long id, TransactionCategoryRequest request);
    
    /**
     * Elimina una categoría
     * @param id ID de la categoría
     */
    void deleteCategory(Long id);
    
    /**
     * Obtiene una categoría por su ID
     * @param id ID de la categoría
     * @return Categoría encontrada
     */
    TransactionCategoryResponse getCategoryById(Long id);
    
    /**
     * Obtiene todas las categorías
     * @return Lista de categorías
     */
    List<TransactionCategoryResponse> getAllCategories();
    
    /**
     * Obtiene todas las categorías principales (sin categoría padre)
     * @return Lista de categorías principales
     */
    List<TransactionCategoryResponse> getRootCategories();
    
    /**
     * Obtiene todas las subcategorías de una categoría
     * @param parentId ID de la categoría padre
     * @return Lista de subcategorías
     */
    List<TransactionCategoryResponse> getSubcategories(Long parentId);
    
    /**
     * Obtiene todas las categorías de tipo gasto
     * @return Lista de categorías de gasto
     */
    List<TransactionCategoryResponse> getExpenseCategories();
    
    /**
     * Obtiene todas las categorías de tipo ingreso
     * @return Lista de categorías de ingreso
     */
    List<TransactionCategoryResponse> getIncomeCategories();
    
    /**
     * Asigna una categoría a una transacción
     * @param request Datos del mapeo
     * @return Mapeo creado
     */
    TransactionCategoryMappingResponse assignCategoryToTransaction(TransactionCategoryMappingRequest request);
    
    /**
     * Elimina la asignación de una categoría a una transacción
     * @param mappingId ID del mapeo
     */
    void removeCategoryFromTransaction(Long mappingId);
    
    /**
     * Obtiene todas las categorías asignadas a una transacción
     * @param transactionId ID de la transacción
     * @return Lista de mapeos de categorías
     */
    List<TransactionCategoryMappingResponse> getTransactionCategories(Long transactionId);
    
    /**
     * Inicializa las categorías predeterminadas del sistema
     */
    void initializeDefaultCategories();
}
