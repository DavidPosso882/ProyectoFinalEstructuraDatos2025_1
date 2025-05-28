package org.example.dto.response;

import lombok.Data;
import org.example.model.TransactionStatus;
import org.example.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String description;
    private WalletResponse sourceWallet;
    private WalletResponse targetWallet;
    private Integer pointsEarned;
    private boolean reversed;
    private String referenceId;
    private TransactionStatus status;
}
