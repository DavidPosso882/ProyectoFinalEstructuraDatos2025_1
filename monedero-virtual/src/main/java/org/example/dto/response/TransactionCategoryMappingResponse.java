package org.example.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionCategoryMappingResponse {
    
    private Long id;
    
    private Long transactionId;
    
    private TransactionCategoryResponse category;
    
    private String notes;
    
    private boolean autoCategorized;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
