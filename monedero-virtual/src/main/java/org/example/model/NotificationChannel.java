package org.example.model;

/**
 * Canales de entrega para notificaciones
 */
public enum NotificationChannel {
    APP("Aplicación", "Notificaciones dentro de la aplicación"),
    EMAIL("Correo Electrónico", "Notificaciones por correo electrónico"),
    SMS("SMS", "Notificaciones por mensaje de texto"),
    PUSH("Push", "Notificaciones push en dispositivos móviles");
    
    private final String name;
    private final String description;
    
    NotificationChannel(String name, String description) {
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
