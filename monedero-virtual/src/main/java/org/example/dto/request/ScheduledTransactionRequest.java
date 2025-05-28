package org.example.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.model.RecurrenceType;
import org.example.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScheduledTransactionRequest {
    @NotNull
    private TransactionType type;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    @Future
    private LocalDateTime scheduledDate;

    private String description;

    @NotNull
    private Long walletId;

    private Long targetWalletId;

    private RecurrenceType recurrenceType;

    // Día específico del mes para recurrencia mensual (1-31)
    @Min(1)
    @Max(31)
    private Integer recurrenceDay;

    // Día de la semana para recurrencia mensual (1-7, donde 1 es lunes)
    @Min(1)
    @Max(7)
    private Integer recurrenceWeekDay;

    // Número máximo de ejecuciones
    @Min(1)
    private Integer recurrenceCount;

    // Fecha de fin de recurrencia
    @Future
    private LocalDateTime recurrenceEndDate;
}
