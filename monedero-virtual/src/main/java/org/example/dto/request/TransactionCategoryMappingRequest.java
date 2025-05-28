package org.example.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionCategoryMappingRequest {
    
    @NotNull(message = "El ID de la transacción es obligatorio")
    private Long transactionId;
    
    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoryId;
    
    private String notes;
}
