package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para solicitudes de creación o actualización de monedas.
 */
@Data
public class CurrencyRequest {
    
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "El código debe ser de 3 letras mayúsculas")
    private String code;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String symbol;
    
    @NotNull
    @Positive
    private BigDecimal exchangeRate;
    
    private boolean baseCurrency;
}
