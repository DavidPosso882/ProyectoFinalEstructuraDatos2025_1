package org.example.repository;

import org.example.model.Notification;
import org.example.model.NotificationType;
import org.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);

    List<Notification> findByUserAndRead(User user, boolean read);

    Page<Notification> findByUserAndRead(User user, boolean read, Pageable pageable);

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Notification> findByType(NotificationType type);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now WHERE n.user.id = :userId AND n.read = false")
    int markAllAsRead(Long userId, LocalDateTime now);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false")
    long countUnreadNotifications(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdAt > :since AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findRecentUnreadNotifications(Long userId, LocalDateTime since);

    /**
     * Encuentra las notificaciones más recientes de un usuario
     * @param userId ID del usuario
     * @param limit Número máximo de notificaciones a devolver
     * @return Lista de notificaciones ordenadas por fecha de creación (más recientes primero)
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Método de conveniencia para obtener un número específico de notificaciones recientes
     */
    default List<Notification> findRecentByUser(Long userId, int limit) {
        return findRecentByUser(userId, Pageable.ofSize(limit));
    }

    /**
     * Encuentra notificaciones pendientes de envío por canales alternativos
     * @return Lista de notificaciones pendientes
     */
    @Query("SELECT n FROM Notification n WHERE n.read = false AND n.createdAt > :since ORDER BY n.createdAt")
    List<Notification> findPendingNotificationsSince(@Param("since") LocalDateTime since);

    /**
     * Método de conveniencia para obtener notificaciones pendientes de las últimas 24 horas
     */
    default List<Notification> findPendingNotifications() {
        return findPendingNotificationsSince(LocalDateTime.now().minusHours(24));
    }

    /**
     * Elimina notificaciones antiguas
     * @param cutoffDate Fecha límite (se eliminarán las anteriores a esta fecha)
     * @return Número de notificaciones eliminadas
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}
