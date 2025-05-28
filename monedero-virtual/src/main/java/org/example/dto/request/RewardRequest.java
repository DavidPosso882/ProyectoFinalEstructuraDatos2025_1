package org.example.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RewardRequest {
    
    @NotBlank(message = "El código de la recompensa es obligatorio")
    private String code;
    
    @NotBlank(message = "El nombre de la recompensa es obligatorio")
    private String name;
    
    private String description;
    
    private String imageUrl;
    
    @NotNull(message = "El costo en puntos es obligatorio")
    @Min(value = 1, message = "El costo en puntos debe ser mayor a cero")
    private Integer pointsCost;
    
    private BigDecimal monetaryValue;
    
    private boolean active = true;
    
    private boolean featured = false;
    
    private Integer stockQuantity;
    
    private Integer maxPerUser;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Long categoryId;
}
