package org.example.dto.response;

import lombok.Data;
import org.example.model.WalletType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal balance;
    private WalletType walletType;
    private CurrencyResponse currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Campos para mostrar flujo de dinero mensual
    private BigDecimal monthlyInflow = BigDecimal.ZERO;
    private BigDecimal monthlyOutflow = BigDecimal.ZERO;

    // Información básica del usuario propietario
    private UserBasicInfo user;

    @Data
    public static class UserBasicInfo {
        private Long id;
        private String username;
    }
}
