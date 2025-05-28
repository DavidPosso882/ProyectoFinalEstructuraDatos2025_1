package org.example.repository;

import org.example.model.NotificationChannel;
import org.example.model.NotificationPreference;
import org.example.model.NotificationType;
import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    
    /**
     * Encuentra todas las preferencias de un usuario
     * @param user Usuario
     * @return Lista de preferencias
     */
    List<NotificationPreference> findByUser(User user);
    
    /**
     * Encuentra todas las preferencias de un usuario por su ID
     * @param userId ID del usuario
     * @return Lista de preferencias
     */
    List<NotificationPreference> findByUserId(Long userId);
    
    /**
     * Encuentra una preferencia específica de un usuario
     * @param user Usuario
     * @param notificationType Tipo de notificación
     * @return Preferencia si existe
     */
    Optional<NotificationPreference> findByUserAndNotificationType(User user, NotificationType notificationType);
    
    /**
     * Encuentra una preferencia específica de un usuario por su ID
     * @param userId ID del usuario
     * @param notificationType Tipo de notificación
     * @return Preferencia si existe
     */
    Optional<NotificationPreference> findByUserIdAndNotificationType(Long userId, NotificationType notificationType);
    
    /**
     * Encuentra todas las preferencias para un canal específico
     * @param deliveryChannel Canal de entrega
     * @return Lista de preferencias
     */
    List<NotificationPreference> findByDeliveryChannel(NotificationChannel deliveryChannel);
    
    /**
     * Encuentra todas las preferencias habilitadas para un tipo de notificación
     * @param notificationType Tipo de notificación
     * @return Lista de preferencias
     */
    List<NotificationPreference> findByNotificationTypeAndEnabledTrue(NotificationType notificationType);
    
    /**
     * Verifica si un usuario tiene habilitada una notificación específica
     * @param userId ID del usuario
     * @param notificationType Tipo de notificación
     * @return true si está habilitada, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(np) > 0 THEN true ELSE false END FROM NotificationPreference np WHERE np.user.id = :userId AND np.notificationType = :notificationType AND np.enabled = true")
    boolean isNotificationEnabled(Long userId, NotificationType notificationType);
    
    /**
     * Encuentra todas las preferencias que no tienen un canal de entrega configurado
     * @return Lista de preferencias
     */
    List<NotificationPreference> findByDeliveryChannelIsNull();
}
