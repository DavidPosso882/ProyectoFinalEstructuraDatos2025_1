package org.example.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta para el análisis de patrones de gasto
 */
@Data
public class SpendingPatternResponse {
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    // Categorías principales de gasto
    private List<CategoryNode> topCategories;
    
    // Secuencias comunes de gasto
    private List<SpendingSequence> commonSequences;
    
    // Correlaciones entre categorías
    private List<CategoryCorrelation> categoryCorrelations;
    
    // Patrones cíclicos de gasto
    private List<CyclicalPattern> cyclicalPatterns;
    
    // Recomendaciones basadas en patrones
    private List<String> recommendations;
    
    // Serie temporal de patrones de gasto por categoría principal
    private List<TimeSeriesCategoryDataPoint> timeSeriesData;
    
    /**
     * Nodo de categoría para el grafo de gastos
     */
    @Data
    public static class CategoryNode {
        private Long categoryId;
        private String categoryName;
        private String iconName;
        private String colorCode;
        private BigDecimal amount;
        private double percentage;
    }
    
    /**
     * Secuencia de gasto entre categorías
     */
    @Data
    public static class SpendingSequence {
        private Long sourceCategoryId;
        private String sourceCategoryName;
        private Long targetCategoryId;
        private String targetCategoryName;
        private int frequency;
    }
    
    /**
     * Correlación entre categorías de gasto
     */
    @Data
    public static class CategoryCorrelation {
        private Long category1Id;
        private String category1Name;
        private Long category2Id;
        private String category2Name;
        private double correlationStrength;
    }
    
    /**
     * Patrón cíclico de gasto
     */
    @Data
    public static class CyclicalPattern {
        private Long categoryId;
        private String categoryName;
        private BigDecimal averageAmount;
        private String frequency; // "Diario", "Semanal", "Mensual", etc.
        private double regularity; // 0.0 a 1.0, donde 1.0 es perfectamente regular
    }

    @Data
    public static class TimeSeriesCategoryDataPoint {
        private LocalDateTime date;
        private Long categoryId;
        private String categoryName;
        private java.math.BigDecimal amount;
        private String transactionType; // "DEPOSIT" o "WITHDRAWAL"
    }
}
