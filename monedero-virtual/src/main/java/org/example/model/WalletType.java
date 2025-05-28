package org.example.model;

/**
 * Tipos de monederos que puede tener un usuario
 */
public enum WalletType {
    PRIMARY("Monedero Principal"),
    SAVINGS("Monedero de Ahorros"),
    EXPENSES("Monedero de Gastos"),
    INVESTMENT("Monedero de Inversiones");

    private final String description;

    WalletType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
