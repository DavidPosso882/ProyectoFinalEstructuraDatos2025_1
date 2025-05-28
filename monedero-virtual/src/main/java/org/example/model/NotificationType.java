package org.example.model;

/**
 * Tipos de notificaciones
 */
public enum NotificationType {
    // Transacciones
    TRANSACTION_COMPLETED("Transacción Completada"),
    TRANSACTION_REVERSED("Transacción Revertida"),
    TRANSACTION_FAILED("Transacción Fallida"),

    // Saldo y límites
    LOW_BALANCE("Saldo Bajo"),
    BALANCE_THRESHOLD_REACHED("Umbral de Saldo Alcanzado"),
    SPENDING_LIMIT_REACHED("Límite de Gasto Alcanzado"),

    // Transacciones programadas
    SCHEDULED_TRANSACTION_REMINDER("Recordatorio de Transacción Programada"),
    SCHEDULED_TRANSACTION_EXECUTED("Transacción Programada Ejecutada"),
    SCHEDULED_TRANSACTION_FAILED("Transacción Programada Fallida"),

    // Sistema de puntos
    POINTS_EARNED("Puntos Ganados"),
    POINTS_REDEEMED("Puntos Canjeados"),
    POINTS_EXPIRING_SOON("Puntos a Punto de Expirar"),
    RANK_CHANGED("Cambio de Rango"),
    RANK_BENEFIT_ACTIVATED("Beneficio de Rango Activado"),

    // Recompensas
    REWARD_REDEEMED("Recompensa Canjeada"),
    REWARD_DELIVERED("Recompensa Entregada"),
    REWARD_CANCELLED("Recompensa Cancelada"),
    REWARD_AVAILABLE("Nueva Recompensa Disponible"),

    // Relaciones entre monederos
    WALLET_RELATION_CREATED("Relación entre Monederos Creada"),
    AUTO_TRANSFER_EXECUTED("Transferencia Automática Ejecutada"),

    // Análisis de gastos
    SPENDING_PATTERN_DETECTED("Patrón de Gasto Detectado"),
    BUDGET_ALERT("Alerta de Presupuesto"),
    UNUSUAL_ACTIVITY("Actividad Inusual Detectada"),
    SAVING_GOAL_PROGRESS("Progreso en Meta de Ahorro"),

    // Seguridad y sistema
    SECURITY_ALERT("Alerta de Seguridad"),
    LOGIN_ATTEMPT("Intento de Inicio de Sesión"),
    PROFILE_UPDATED("Perfil Actualizado"),
    SYSTEM("Sistema");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
