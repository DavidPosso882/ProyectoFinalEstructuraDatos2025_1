package org.example.dto.response;

import lombok.Data;
import org.example.model.RecurrenceType;
import org.example.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScheduledTransactionResponse {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime scheduledDate;
    private LocalDateTime creationDate;
    private String description;
    private WalletResponse wallet;
    private Long targetWalletId;
    private RecurrenceType recurrenceType;
    private Integer recurrenceDay;
    private Integer recurrenceWeekDay;
    private Integer recurrenceCount;
    private LocalDateTime recurrenceEndDate;
    private boolean executed;
    private LocalDateTime executionDate;
    private Integer executionCount;
    private LocalDateTime nextExecutionDate;
}
