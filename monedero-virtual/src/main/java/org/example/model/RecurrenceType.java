package org.example.model;

/**
 * Tipos de recurrencia para transacciones programadas
 */
public enum RecurrenceType {
    ONCE("Una vez"),
    DAILY("Diaria"),
    WEEKLY("Semanal"),
    BIWEEKLY("Quincenal"),
    MONTHLY("Mensual"),
    QUARTERLY("Trimestral"),
    YEARLY("Anual");

    private final String description;

    RecurrenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
