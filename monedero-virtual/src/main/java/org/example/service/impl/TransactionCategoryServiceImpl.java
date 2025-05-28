package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.dto.request.TransactionCategoryMappingRequest;
import org.example.dto.request.TransactionCategoryRequest;
import org.example.dto.response.TransactionCategoryMappingResponse;
import org.example.dto.response.TransactionCategoryResponse;
import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionCategoryMapping;
import org.example.repository.TransactionCategoryMappingRepository;
import org.example.repository.TransactionCategoryRepository;
import org.example.repository.TransactionRepository;
import org.example.service.TransactionCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionCategoryServiceImpl implements TransactionCategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionCategoryServiceImpl.class);
    
    @Autowired
    private TransactionCategoryRepository categoryRepository;
    
    @Autowired
    private TransactionCategoryMappingRepository mappingRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Override
    @Transactional
    public TransactionCategoryResponse createCategory(TransactionCategoryRequest request) {
        // Verificar si ya existe una categoría con el mismo nombre
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + request.getName());
        }
        
        TransactionCategory category = new TransactionCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconName(request.getIconName());
        category.setColorCode(request.getColorCode());
        category.setExpense(request.isExpense());
        
        // Si tiene categoría padre, establecerla
        if (request.getParentCategoryId() != null) {
            TransactionCategory parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría padre no encontrada"));
            category.setParentCategory(parentCategory);
        }
        
        TransactionCategory savedCategory = categoryRepository.save(category);
        logger.info("Categoría creada: {}", savedCategory.getName());
        
        return convertToCategoryResponse(savedCategory);
    }
    
    @Override
    @Transactional
    public TransactionCategoryResponse updateCategory(Long id, TransactionCategoryRequest request) {
        TransactionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        
        // No permitir actualizar categorías del sistema
        if (category.isSystem()) {
            throw new IllegalArgumentException("No se pueden modificar las categorías del sistema");
        }
        
        // Verificar si el nombre ya está en uso por otra categoría
        if (!category.getName().equals(request.getName()) && 
                categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + request.getName());
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconName(request.getIconName());
        category.setColorCode(request.getColorCode());
        category.setExpense(request.isExpense());
        
        // Actualizar categoría padre si es necesario
        if (request.getParentCategoryId() != null) {
            // Evitar ciclos en la jerarquía de categorías
            if (request.getParentCategoryId().equals(id)) {
                throw new IllegalArgumentException("Una categoría no puede ser su propia categoría padre");
            }
            
            TransactionCategory parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría padre no encontrada"));
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }
        
        TransactionCategory updatedCategory = categoryRepository.save(category);
        logger.info("Categoría actualizada: {}", updatedCategory.getName());
        
        return convertToCategoryResponse(updatedCategory);
    }
    
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        TransactionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        
        // No permitir eliminar categorías del sistema
        if (category.isSystem()) {
            throw new IllegalArgumentException("No se pueden eliminar las categorías del sistema");
        }
        
        // Verificar si tiene subcategorías
        if (!category.getSubcategories().isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar una categoría que tiene subcategorías");
        }
        
        // Eliminar todos los mapeos de esta categoría
        mappingRepository.deleteByCategory(category);
        
        categoryRepository.delete(category);
        logger.info("Categoría eliminada: {}", category.getName());
    }
    
    @Override
    public TransactionCategoryResponse getCategoryById(Long id) {
        TransactionCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        
        return convertToCategoryResponse(category);
    }
    
    @Override
    public List<TransactionCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionCategoryResponse> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionCategoryResponse> getSubcategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId).stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionCategoryResponse> getExpenseCategories() {
        return categoryRepository.findByExpenseTrue().stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionCategoryResponse> getIncomeCategories() {
        return categoryRepository.findByExpenseFalse().stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public TransactionCategoryMappingResponse assignCategoryToTransaction(TransactionCategoryMappingRequest request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));
        
        TransactionCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        
        // Crear el mapeo
        TransactionCategoryMapping mapping = new TransactionCategoryMapping();
        mapping.setTransaction(transaction);
        mapping.setCategory(category);
        mapping.setNotes(request.getNotes());
        
        TransactionCategoryMapping savedMapping = mappingRepository.save(mapping);
        logger.info("Categoría asignada a transacción: {} -> {}", category.getName(), transaction.getId());
        
        return convertToMappingResponse(savedMapping);
    }
    
    @Override
    @Transactional
    public void removeCategoryFromTransaction(Long mappingId) {
        TransactionCategoryMapping mapping = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new EntityNotFoundException("Mapeo de categoría no encontrado"));
        
        mappingRepository.delete(mapping);
        logger.info("Categoría eliminada de transacción: {} -> {}", 
                mapping.getCategory().getName(), mapping.getTransaction().getId());
    }
    
    @Override
    public List<TransactionCategoryMappingResponse> getTransactionCategories(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));
        
        return mappingRepository.findByTransaction(transaction).stream()
                .map(this::convertToMappingResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void initializeDefaultCategories() {
        // Verificar si ya existen categorías
        if (categoryRepository.count() > 0) {
            logger.info("Las categorías ya están inicializadas");
            return;
        }
        
        logger.info("Inicializando categorías predeterminadas");
        
        // Categorías de gastos
        createSystemCategory("Alimentación", "Gastos en comida y bebida", "food", "#4CAF50", true, null);
        createSystemCategory("Transporte", "Gastos en transporte público, gasolina, etc.", "transport", "#2196F3", true, null);
        createSystemCategory("Vivienda", "Gastos relacionados con la vivienda", "home", "#9C27B0", true, null);
        createSystemCategory("Servicios", "Gastos en servicios públicos", "utilities", "#FF9800", true, null);
        createSystemCategory("Salud", "Gastos médicos y de salud", "health", "#F44336", true, null);
        createSystemCategory("Educación", "Gastos en educación y formación", "education", "#3F51B5", true, null);
        createSystemCategory("Entretenimiento", "Gastos en ocio y entretenimiento", "entertainment", "#E91E63", true, null);
        createSystemCategory("Compras", "Gastos en compras diversas", "shopping", "#009688", true, null);
        createSystemCategory("Otros gastos", "Otros gastos no categorizados", "other", "#607D8B", true, null);
        
        // Categorías de ingresos
        createSystemCategory("Salario", "Ingresos por trabajo", "salary", "#4CAF50", false, null);
        createSystemCategory("Inversiones", "Ingresos por inversiones", "investment", "#2196F3", false, null);
        createSystemCategory("Regalos", "Ingresos por regalos recibidos", "gift", "#9C27B0", false, null);
        createSystemCategory("Otros ingresos", "Otros ingresos no categorizados", "other", "#607D8B", false, null);
        
        logger.info("Categorías predeterminadas inicializadas correctamente");
    }
    
    /**
     * Crea una categoría del sistema
     */
    private TransactionCategory createSystemCategory(String name, String description, String iconName, 
                                                    String colorCode, boolean expense, TransactionCategory parent) {
        TransactionCategory category = new TransactionCategory();
        category.setName(name);
        category.setDescription(description);
        category.setIconName(iconName);
        category.setColorCode(colorCode);
        category.setExpense(expense);
        category.setSystem(true);
        category.setParentCategory(parent);
        
        return categoryRepository.save(category);
    }
    
    /**
     * Convierte una entidad TransactionCategory a un DTO TransactionCategoryResponse
     */
    private TransactionCategoryResponse convertToCategoryResponse(TransactionCategory category) {
        TransactionCategoryResponse response = new TransactionCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setIconName(category.getIconName());
        response.setColorCode(category.getColorCode());
        response.setExpense(category.isExpense());
        response.setSystem(category.isSystem());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        // Establecer información de la categoría padre si existe
        if (category.getParentCategory() != null) {
            response.setParentCategoryId(category.getParentCategory().getId());
            response.setParentCategoryName(category.getParentCategory().getName());
        }
        
        // Añadir subcategorías si existen
        if (!category.getSubcategories().isEmpty()) {
            response.setSubcategories(category.getSubcategories().stream()
                    .map(this::convertToCategoryResponse)
                    .collect(Collectors.toList()));
        }
        
        // Contar transacciones asociadas
        response.setTransactionCount(categoryRepository.countTransactionsByCategoryId(category.getId()));
        
        return response;
    }
    
    /**
     * Convierte una entidad TransactionCategoryMapping a un DTO TransactionCategoryMappingResponse
     */
    private TransactionCategoryMappingResponse convertToMappingResponse(TransactionCategoryMapping mapping) {
        TransactionCategoryMappingResponse response = new TransactionCategoryMappingResponse();
        response.setId(mapping.getId());
        response.setTransactionId(mapping.getTransaction().getId());
        response.setCategory(convertToCategoryResponse(mapping.getCategory()));
        response.setNotes(mapping.getNotes());
        response.setAutoCategorized(mapping.isAutoCategorized());
        response.setCreatedAt(mapping.getCreatedAt());
        response.setUpdatedAt(mapping.getUpdatedAt());
        
        return response;
    }
}
