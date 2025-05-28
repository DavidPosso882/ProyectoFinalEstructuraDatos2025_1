package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RewardRedemptionRequest {
    
    @NotBlank(message = "El código de la recompensa es obligatorio")
    private String rewardCode;
    
    private String notes;
}
