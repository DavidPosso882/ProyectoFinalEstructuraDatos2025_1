package org.example.dto.response;

import lombok.Data;
import org.example.model.RewardRedemption;

import java.time.LocalDateTime;

@Data
public class RewardRedemptionResponse {
    
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private RewardResponse reward;
    
    private Integer pointsSpent;
    
    private LocalDateTime redemptionDate;
    
    private String redemptionCode;
    
    private RewardRedemption.RedemptionStatus status;
    
    private String statusDescription;
    
    private LocalDateTime deliveryDate;
    
    private String notes;
    
    private Long pointsTransactionId;
}
