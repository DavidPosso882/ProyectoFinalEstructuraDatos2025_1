package org.example.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuestas con información de monedas.
 */
@Data
public class CurrencyResponse {
    private Long id;
    private String code;
    private String name;
    private String symbol;
    private BigDecimal exchangeRate;
    private boolean baseCurrency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
