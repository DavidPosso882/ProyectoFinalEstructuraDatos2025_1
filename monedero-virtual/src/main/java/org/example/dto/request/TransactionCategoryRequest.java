package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TransactionCategoryRequest {
    
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String name;
    
    private String description;
    
    private String iconName;
    
    private String colorCode;
    
    private boolean expense = true;
    
    private Long parentCategoryId;
}
