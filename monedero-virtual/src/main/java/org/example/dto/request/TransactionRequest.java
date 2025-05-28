package org.example.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.model.TransactionType;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotNull
    private TransactionType type;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String description;

    private Long sourceWalletId;

    private Long targetWalletId;
}
