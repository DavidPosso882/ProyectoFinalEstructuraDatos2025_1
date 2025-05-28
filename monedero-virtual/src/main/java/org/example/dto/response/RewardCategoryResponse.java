package org.example.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RewardCategoryResponse {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String iconName;
    
    private String colorCode;
    
    private Integer displayOrder;
    
    private boolean active;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private List<RewardResponse> rewards = new ArrayList<>();
    
    private int rewardCount;
}
