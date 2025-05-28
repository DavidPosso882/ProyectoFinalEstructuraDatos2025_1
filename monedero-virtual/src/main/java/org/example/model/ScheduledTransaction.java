package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Entity
@Table(name = "scheduled_transactions")
@Data
@NoArgsConstructor
public class ScheduledTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "target_wallet_id")
    private Long targetWalletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type")
    private RecurrenceType recurrenceType;

    @Column(name = "recurrence_day")
    private Integer recurrenceDay;

    @Column(name = "recurrence_week_day")
    private Integer recurrenceWeekDay;

    @Column(name = "recurrence_count")
    private Integer recurrenceCount;

    @Column(name = "recurrence_end_date")
    private LocalDateTime recurrenceEndDate;

    @Column(name = "is_executed")
    private boolean executed = false;

    @Column(name = "execution_date")
    private LocalDateTime executionDate;

    @Column(name = "execution_count")
    private Integer executionCount = 0;

    @PrePersist
    protected void onCreate() {
        creationDate = LocalDateTime.now();
    }

    /**
     * Marca esta transacción programada como ejecutada
     */
    public void markAsExecuted() {
        this.executed = true;
        this.executionDate = LocalDateTime.now();

        if (this.executionCount == null) {
            this.executionCount = 1;
        } else {
            this.executionCount++;
        }
    }

    /**
     * Verifica si esta transacción programada debe ejecutarse ahora
     */
    public boolean shouldExecuteNow() {
        // Si ya está ejecutada, no debe ejecutarse de nuevo (a menos que sea recurrente)
        if (executed && (recurrenceType == null || recurrenceType == RecurrenceType.ONCE)) {
            return false;
        }

        // Si es recurrente y ha alcanzado el número máximo de ejecuciones, no debe ejecutarse
        if (recurrenceCount != null && executionCount != null && executionCount >= recurrenceCount) {
            return false;
        }

        // Si es recurrente y ha pasado la fecha de fin, no debe ejecutarse
        if (recurrenceEndDate != null && LocalDateTime.now().isAfter(recurrenceEndDate)) {
            return false;
        }

        return scheduledDate.isBefore(LocalDateTime.now()) || scheduledDate.isEqual(LocalDateTime.now());
    }

    /**
     * Calcula la próxima fecha de ejecución basada en el tipo de recurrencia
     */
    public LocalDateTime calculateNextExecutionDate() {
        if (recurrenceType == null || recurrenceType == RecurrenceType.ONCE) {
            return null;
        }

        // Si ha alcanzado el número máximo de ejecuciones, no hay próxima fecha
        if (recurrenceCount != null && executionCount != null && executionCount >= recurrenceCount) {
            return null;
        }

        LocalDateTime baseDate = executionDate != null ? executionDate : scheduledDate;
        LocalDateTime nextDate;

        switch (recurrenceType) {
            case DAILY:
                nextDate = baseDate.plusDays(1);
                break;
            case WEEKLY:
                nextDate = baseDate.plusWeeks(1);
                break;
            case BIWEEKLY:
                nextDate = baseDate.plusWeeks(2);
                break;
            case MONTHLY:
                // Si se especificó un día del mes específico
                if (recurrenceDay != null && recurrenceDay > 0 && recurrenceDay <= 31) {
                    nextDate = baseDate.plusMonths(1)
                        .withDayOfMonth(Math.min(recurrenceDay, baseDate.plusMonths(1).getMonth().maxLength()));
                }
                // Si se especificó un día de la semana específico (ej: segundo lunes)
                else if (recurrenceWeekDay != null && recurrenceDay != null) {
                    DayOfWeek dayOfWeek = DayOfWeek.of(recurrenceWeekDay);
                    nextDate = baseDate.plusMonths(1)
                        .with(TemporalAdjusters.firstDayOfMonth())
                        .with(TemporalAdjusters.nextOrSame(dayOfWeek));

                    // Ajustar a la semana específica (recurrenceDay: 1-5 para primera a quinta semana)
                    nextDate = nextDate.plusWeeks(recurrenceDay - 1);

                    // Si la fecha resultante está en el mes siguiente, retroceder una semana
                    if (nextDate.getMonth() != baseDate.plusMonths(1).getMonth()) {
                        nextDate = nextDate.minusWeeks(1);
                    }
                } else {
                    // Por defecto, mismo día del mes siguiente
                    nextDate = baseDate.plusMonths(1);
                }
                break;
            case QUARTERLY:
                nextDate = baseDate.plusMonths(3);
                break;
            case YEARLY:
                nextDate = baseDate.plusYears(1);
                break;
            default:
                return null;
        }

        // Verificar si la próxima fecha está después de la fecha de fin
        if (recurrenceEndDate != null && nextDate.isAfter(recurrenceEndDate)) {
            return null;
        }

        return nextDate;
    }

    /**
     * Verifica si la transacción programada ha terminado su ciclo de recurrencia
     */
    public boolean hasCompletedRecurrence() {
        // Si no es recurrente, se completa después de una ejecución
        if (recurrenceType == null || recurrenceType == RecurrenceType.ONCE) {
            return executed;
        }

        // Si tiene un número máximo de ejecuciones y lo ha alcanzado
        if (recurrenceCount != null && executionCount != null && executionCount >= recurrenceCount) {
            return true;
        }

        // Si tiene una fecha de fin y ya ha pasado
        if (recurrenceEndDate != null && LocalDateTime.now().isAfter(recurrenceEndDate)) {
            return true;
        }

        return false;
    }
}
