package org.example.service;

import org.example.dto.request.NotificationPreferenceRequest;
import org.example.dto.response.NotificationPreferenceResponse;
import org.example.model.NotificationChannel;
import org.example.model.NotificationType;

import java.util.List;

public interface NotificationPreferenceService {
    
    /**
     * Obtiene todas las preferencias de notificación de un usuario
     * @param userId ID del usuario
     * @return Lista de preferencias
     */
    List<NotificationPreferenceResponse> getUserNotificationPreferences(Long userId);
    
    /**
     * Obtiene una preferencia específica de un usuario
     * @param userId ID del usuario
     * @param notificationType Tipo de notificación
     * @return Preferencia si existe
     */
    NotificationPreferenceResponse getUserNotificationPreference(Long userId, NotificationType notificationType);
    
    /**
     * Actualiza una preferencia de notificación
     * @param userId ID del usuario
     * @param notificationType Tipo de notificación
     * @param request Datos de la preferencia
     * @return Preferencia actualizada
     */
    NotificationPreferenceResponse updateNotificationPreference(Long userId, NotificationType notificationType, NotificationPreferenceRequest request);
    
    /**
     * Actualiza todas las preferencias de notificación de un usuario
     * @param userId ID del usuario
     * @param requests Lista de preferencias
     * @return Lista de preferencias actualizadas
     */
    List<NotificationPreferenceResponse> updateAllNotificationPreferences(Long userId, List<NotificationPreferenceRequest> requests);
    
    /**
     * Habilita o deshabilita una preferencia de notificación
     * @param userId ID del usuario
     * @param notificationType Tipo de notificación
     * @param enabled true para habilitar, false para deshabilitar
     * @return Preferencia actualizada
     */
    NotificationPreferenceResponse setNotificationEnabled(Long userId, NotificationType notificationType, boolean enabled);
    
    /**
     * Configura el canal de entrega para una preferencia de notificación
     * @param userId ID del usuario
     * @param notificationType Tipo de notificación
     * @param channel Canal de entrega
     * @return Preferencia actualizada
     */
    NotificationPreferenceResponse setDeliveryChannel(Long userId, NotificationType notificationType, NotificationChannel channel);
    
    /**
     * Configura las horas de silencio para una preferencia de notificación
     * @param userId ID del usuario
     * @param notificationType Tipo de notificación
     * @param startHour Hora de inicio (0-23)
     * @param endHour Hora de fin (0-23)
     * @return Preferencia actualizada
     */
    NotificationPreferenceResponse setQuietHours(Long userId, NotificationType notificationType, Integer startHour, Integer endHour);
    
    /**
     * Verifica si un usuario debe recibir una notificación específica
     * @param userId ID del usuario
     * @param notificationType Tipo de notificación
     * @return true si debe recibir la notificación, false en caso contrario
     */
    boolean shouldReceiveNotification(Long userId, NotificationType notificationType);
    
    /**
     * Inicializa las preferencias de notificación para un nuevo usuario
     * @param userId ID del usuario
     */
    void initializeUserPreferences(Long userId);
}
