package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa una categoría de transacción.
 * Las categorías permiten clasificar las transacciones para análisis de patrones de gasto.
 */
@Entity
@Table(name = "transaction_categories")
@Data
@NoArgsConstructor
public class TransactionCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column
    private String description;
    
    @Column(name = "icon_name")
    private String iconName;
    
    @Column(name = "color_code")
    private String colorCode;
    
    @Column(name = "is_expense")
    private boolean expense = true;
    
    @Column(name = "is_system")
    private boolean system = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private TransactionCategory parentCategory;
    
    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private List<TransactionCategory> subcategories = new ArrayList<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<TransactionCategoryMapping> transactions = new ArrayList<>();
    
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
     * Añade una subcategoría a esta categoría
     */
    public void addSubcategory(TransactionCategory subcategory) {
        subcategories.add(subcategory);
        subcategory.setParentCategory(this);
    }
    
    /**
     * Elimina una subcategoría de esta categoría
     */
    public void removeSubcategory(TransactionCategory subcategory) {
        subcategories.remove(subcategory);
        subcategory.setParentCategory(null);
    }
}
