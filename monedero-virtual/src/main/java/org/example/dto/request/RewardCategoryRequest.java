package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RewardCategoryRequest {
    
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String name;
    
    private String description;
    
    private String iconName;
    
    private String colorCode;
    
    private Integer displayOrder;
    
    private boolean active = true;
}
