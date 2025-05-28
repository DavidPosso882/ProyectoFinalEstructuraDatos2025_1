package org.example.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.model.WalletRelationType;

import java.math.BigDecimal;

@Data
public class WalletRelationRequest {
    
    @NotNull
    private Long sourceWalletId;
    
    @NotNull
    private Long targetWalletId;
    
    @NotNull
    private WalletRelationType relationType;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal autoTransferPercentage;
    
    @DecimalMin(value = "0.01")
    private BigDecimal autoTransferThreshold;
    
    private boolean autoTransferEnabled = false;
    
    private String description;
}
