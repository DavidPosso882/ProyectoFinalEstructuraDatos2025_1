package org.example.model;

/**
 * Tipos de transacciones de puntos
 */
public enum PointsTransactionType {
    EARNED("Puntos Ganados"),
    REDEEMED("Puntos Canjeados"),
    EXPIRED("Puntos Expirados"),
    ADJUSTED("Puntos Ajustados");

    private final String description;

    PointsTransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
