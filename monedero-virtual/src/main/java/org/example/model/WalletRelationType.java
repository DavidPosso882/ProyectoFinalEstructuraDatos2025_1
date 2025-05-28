package org.example.model;

/**
 * Tipos de relaciones entre monederos
 */
public enum WalletRelationType {
    PARENT_CHILD("Padre-Hijo", "Relación jerárquica donde un monedero es el principal y otro es dependiente"),
    SAVINGS("Ahorro", "Relación para transferir automáticamente un porcentaje a un monedero de ahorro"),
    EXPENSE_ALLOCATION("Asignación de Gastos", "Relación para asignar fondos a un monedero de gastos específico"),
    INVESTMENT("Inversión", "Relación para transferir fondos a un monedero de inversión"),
    CUSTOM("Personalizada", "Relación personalizada definida por el usuario");
    
    private final String name;
    private final String description;
    
    WalletRelationType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}
