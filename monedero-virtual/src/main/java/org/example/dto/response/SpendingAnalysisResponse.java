package org.example.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SpendingAnalysisResponse {
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private BigDecimal totalExpenses;
    
    private BigDecimal totalIncome;
    
    private BigDecimal netCashflow;
    
    private List<CategorySpendingDTO> categoryBreakdown = new ArrayList<>();
    
    private List<TimeSeriesDataPoint> timeSeriesData = new ArrayList<>();
    
    private List<SpendingTrendDTO> trends = new ArrayList<>();
    
    @Data
    public static class CategorySpendingDTO {
        private Long categoryId;
        private String categoryName;
        private String iconName;
        private String colorCode;
        private BigDecimal amount;
        private double percentage;
    }
    
    @Data
    public static class TimeSeriesDataPoint {
        private LocalDateTime date;
        private BigDecimal expenses;
        private BigDecimal income;
    }
    
    @Data
    public static class SpendingTrendDTO {
        private String description;
        private BigDecimal amount;
        private double percentageChange;
        private boolean increase;
    }
}
