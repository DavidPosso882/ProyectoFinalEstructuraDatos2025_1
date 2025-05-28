package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.model.WalletType;
import java.math.BigDecimal;

@Data
public class WalletRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    private WalletType walletType;

    private String currencyCode;

    private BigDecimal initialBalance;
}
