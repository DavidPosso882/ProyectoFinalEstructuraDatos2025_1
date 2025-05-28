package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.TransactionCategoryMappingRequest;
import org.example.dto.request.TransactionCategoryRequest;
import org.example.dto.response.ApiResponse;
import org.example.dto.response.TransactionCategoryMappingResponse;
import org.example.dto.response.TransactionCategoryResponse;
import org.example.security.CurrentUser;
import org.example.security.UserPrincipal;
import org.example.service.TransactionCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class TransactionCategoryController {
    
    @Autowired
    private TransactionCategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<TransactionCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
    
    @GetMapping("/root")
    public ResponseEntity<List<TransactionCategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }
    
    @GetMapping("/expense")
    public ResponseEntity<List<TransactionCategoryResponse>> getExpenseCategories() {
        return ResponseEntity.ok(categoryService.getExpenseCategories());
    }
    
    @GetMapping("/income")
    public ResponseEntity<List<TransactionCategoryResponse>> getIncomeCategories() {
        return ResponseEntity.ok(categoryService.getIncomeCategories());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionCategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
    
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<TransactionCategoryResponse>> getSubcategories(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getSubcategories(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionCategoryResponse> createCategory(
            @Valid @RequestBody TransactionCategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionCategoryResponse> updateCategory(
            @PathVariable Long id, @Valid @RequestBody TransactionCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse(true, "Categoría eliminada correctamente"));
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<TransactionCategoryMappingResponse>> getTransactionCategories(
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(categoryService.getTransactionCategories(transactionId));
    }
    
    @PostMapping("/transaction")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransactionCategoryMappingResponse> assignCategoryToTransaction(
            @Valid @RequestBody TransactionCategoryMappingRequest request,
            @CurrentUser UserPrincipal currentUser) {
        // Aquí se debería verificar que el usuario es propietario de la transacción
        return ResponseEntity.ok(categoryService.assignCategoryToTransaction(request));
    }
    
    @DeleteMapping("/transaction/mapping/{mappingId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> removeCategoryFromTransaction(
            @PathVariable Long mappingId,
            @CurrentUser UserPrincipal currentUser) {
        // Aquí se debería verificar que el usuario es propietario de la transacción
        categoryService.removeCategoryFromTransaction(mappingId);
        return ResponseEntity.ok(new ApiResponse(true, "Categoría eliminada de la transacción correctamente"));
    }
    
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> initializeDefaultCategories() {
        categoryService.initializeDefaultCategories();
        return ResponseEntity.ok(new ApiResponse(true, "Categorías predeterminadas inicializadas correctamente"));
    }
}
