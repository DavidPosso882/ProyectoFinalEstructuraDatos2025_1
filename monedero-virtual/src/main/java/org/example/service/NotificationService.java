package org.example.service;

import org.example.dto.response.NotificationResponse;
import org.example.model.Notification;
import org.example.model.NotificationChannel;
import org.example.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Crea una notificación para un usuario
     * @param userId ID del usuario
     * @param title Título de la notificación
     * @param message Mensaje de la notificación
     * @param type Tipo de notificación
     * @param relatedEntityId ID de la entidad relacionada (opcional)
     * @param relatedEntityType Tipo de entidad relacionada (opcional)
     * @return Notificación creada
     */
    Notification createNotification(Long userId, String title, String message, NotificationType type,
                                   Long relatedEntityId, Notification.RelatedEntityType relatedEntityType);

    /**
     * Obtiene las notificaciones de un usuario
     * @param userId ID del usuario
     * @param pageable Paginación
     * @return Página de notificaciones
     */
    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

    /**
     * Obtiene las notificaciones no leídas de un usuario
     * @param userId ID del usuario
     * @param pageable Paginación
     * @return Página de notificaciones no leídas
     */
    Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable);

    /**
     * Marca una notificación como leída
     * @param notificationId ID de la notificación
     * @param userId ID del usuario propietario
     * @return Notificación actualizada
     */
    NotificationResponse markNotificationAsRead(Long notificationId, Long userId);

    /**
     * Marca todas las notificaciones de un usuario como leídas
     * @param userId ID del usuario
     * @return Número de notificaciones marcadas como leídas
     */
    int markAllNotificationsAsRead(Long userId);

    /**
     * Elimina una notificación
     * @param notificationId ID de la notificación
     * @param userId ID del usuario propietario
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * Cuenta las notificaciones no leídas de un usuario
     * @param userId ID del usuario
     * @return Número de notificaciones no leídas
     */
    long countUnreadNotifications(Long userId);

    /**
     * Crea una notificación para un usuario, respetando sus preferencias
     * @param userId ID del usuario
     * @param title Título de la notificación
     * @param message Mensaje de la notificación
     * @param type Tipo de notificación
     * @param relatedEntityId ID de la entidad relacionada (opcional)
     * @param relatedEntityType Tipo de entidad relacionada (opcional)
     * @return Notificación creada, o null si el usuario ha desactivado este tipo de notificación
     */
    Notification createNotificationWithPreferences(Long userId, String title, String message, NotificationType type,
                                                 Long relatedEntityId, Notification.RelatedEntityType relatedEntityType);

    /**
     * Envía una notificación a través de un canal específico
     * @param notificationId ID de la notificación
     * @param channel Canal de entrega
     * @return true si se envió correctamente, false en caso contrario
     */
    boolean sendNotificationThroughChannel(Long notificationId, NotificationChannel channel);

    /**
     * Envía todas las notificaciones pendientes a través de sus canales configurados
     * @return Número de notificaciones enviadas
     */
    int processPendingNotifications();

    /**
     * Elimina notificaciones antiguas
     * @param daysOld Número de días de antigüedad
     * @return Número de notificaciones eliminadas
     */
    int cleanupOldNotifications(int daysOld);
}
