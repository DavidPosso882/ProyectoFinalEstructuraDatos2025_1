package org.example.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionCategoryResponse {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String iconName;
    
    private String colorCode;
    
    private boolean expense;
    
    private boolean system;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long parentCategoryId;
    
    private String parentCategoryName;
    
    private List<TransactionCategoryResponse> subcategories = new ArrayList<>();
    
    private long transactionCount;
}
