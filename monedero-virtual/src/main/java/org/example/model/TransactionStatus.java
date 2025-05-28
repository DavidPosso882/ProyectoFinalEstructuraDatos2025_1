package org.example.model;

/**
 * Estados posibles de una transacción
 */
public enum TransactionStatus {
    PENDING("Pendiente"),
    COMPLETED("Completada"),
    FAILED("Fallida"),
    REVERSED("Reversada"),
    SCHEDULED("Programada");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
