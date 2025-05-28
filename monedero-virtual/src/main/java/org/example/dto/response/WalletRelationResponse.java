package org.example.dto.response;

import lombok.Data;
import org.example.model.WalletRelationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletRelationResponse {
    private Long id;
    private WalletResponse sourceWallet;
    private WalletResponse targetWallet;
    private WalletRelationType relationType;
    private String relationTypeName;
    private String relationTypeDescription;
    private BigDecimal autoTransferPercentage;
    private BigDecimal autoTransferThreshold;
    private boolean autoTransferEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
