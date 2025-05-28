package org.example.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RewardResponse {
    
    private Long id;
    
    private String code;
    
    private String name;
    
    private String description;
    
    private String imageUrl;
    
    private Integer pointsCost;
    
    private BigDecimal monetaryValue;
    
    private boolean active;
    
    private boolean featured;
    
    private Integer stockQuantity;
    
    private Integer maxPerUser;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long categoryId;
    
    private String categoryName;
    
    private boolean available;
    
    private int redemptionCount;
    
    private int userRedemptionCount;
    
    private boolean canRedeem;
}
