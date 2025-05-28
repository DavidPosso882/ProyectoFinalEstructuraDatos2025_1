package org.example.model;

/**
 * Tipos de transacciones que se pueden realizar en el sistema
 */
public enum TransactionType {
    DEPOSIT("Depósito"),
    WITHDRAWAL("Retiro"),
    TRANSFER("Transferencia"),
    POINTS_REDEMPTION("Canje de Puntos"),
    SCHEDULED_TRANSFER("Transferencia Programada"),
    SYSTEM("Sistema");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
