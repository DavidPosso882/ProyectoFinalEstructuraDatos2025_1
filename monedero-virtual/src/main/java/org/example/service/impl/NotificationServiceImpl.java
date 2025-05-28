package org.example.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.example.datastructure.list.CircularLinkedList;
import org.example.dto.response.NotificationResponse;
import org.example.model.Notification;
import org.example.model.NotificationChannel;
import org.example.model.NotificationType;
import org.example.model.User;
import org.example.repository.NotificationRepository;
import org.example.repository.UserRepository;
import org.example.service.NotificationPreferenceService;
import org.example.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired(required = false)
    private JavaMailSender emailSender;

    // Mapa de listas circulares de notificaciones por usuario
    private Map<Long, CircularLinkedList<Notification>> userNotificationsCache = new HashMap<>();

    // Tamaño máximo de la caché de notificaciones por usuario
    private static final int MAX_CACHED_NOTIFICATIONS = 50;

    @PostConstruct
    public void init() {
        // Cargar notificaciones recientes para usuarios activos
        loadRecentNotifications();
    }

    /**
     * Carga las notificaciones recientes de usuarios activos en la caché
     */
    private void loadRecentNotifications() {
        logger.info("Cargando notificaciones recientes en la caché");

        // Obtener usuarios con notificaciones recientes (últimos 7 días)
        List<User> activeUsers = userRepository.findUsersWithRecentActivity(
            LocalDateTime.now().minusDays(7));

        for (User user : activeUsers) {
            // Obtener las últimas MAX_CACHED_NOTIFICATIONS notificaciones del usuario
            List<Notification> recentNotifications =
                notificationRepository.findRecentByUser(user.getId(), MAX_CACHED_NOTIFICATIONS);

            if (!recentNotifications.isEmpty()) {
                CircularLinkedList<Notification> userList = new CircularLinkedList<>();
                for (Notification notification : recentNotifications) {
                    userList.add(notification);
                }
                userNotificationsCache.put(user.getId(), userList);
            }
        }

        logger.info("Caché de notificaciones cargada para {} usuarios", userNotificationsCache.size());
    }

    /**
     * Tarea programada para limpiar la caché de notificaciones
     */
    @Scheduled(cron = "0 0 3 * * *") // Ejecutar a las 3 AM todos los días
    public void cleanupNotificationsCache() {
        logger.info("Limpiando caché de notificaciones");

        // Eliminar entradas antiguas (usuarios que no han accedido recientemente)
        List<Long> usersToRemove = new ArrayList<>();

        for (Map.Entry<Long, CircularLinkedList<Notification>> entry : userNotificationsCache.entrySet()) {
            Long userId = entry.getKey();

            // Verificar si el usuario ha tenido actividad reciente
            boolean hasRecentActivity = userRepository.hasUserRecentActivity(
                userId, LocalDateTime.now().minusDays(14));

            if (!hasRecentActivity) {
                usersToRemove.add(userId);
            }
        }

        // Eliminar usuarios inactivos de la caché
        for (Long userId : usersToRemove) {
            userNotificationsCache.remove(userId);
        }

        logger.info("Se eliminaron {} usuarios inactivos de la caché", usersToRemove.size());
    }

    @Override
    @Transactional
    public Notification createNotification(Long userId, String title, String message, NotificationType type,
                                          Long relatedEntityId, Notification.RelatedEntityType relatedEntityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);

        // Guardar en la base de datos
        Notification savedNotification = notificationRepository.save(notification);

        // Añadir a la caché de lista circular
        addToUserNotificationsCache(userId, savedNotification);

        return savedNotification;
    }

    /**
     * Añade una notificación a la caché de lista circular del usuario
     */
    private void addToUserNotificationsCache(Long userId, Notification notification) {
        // Obtener o crear la lista circular para el usuario
        CircularLinkedList<Notification> userNotifications = userNotificationsCache.get(userId);

        if (userNotifications == null) {
            userNotifications = new CircularLinkedList<>();
            userNotificationsCache.put(userId, userNotifications);
        }

        // Añadir la notificación a la lista
        userNotifications.add(notification);

        // Si la lista excede el tamaño máximo, eliminar la notificación más antigua
        if (userNotifications.size() > MAX_CACHED_NOTIFICATIONS) {
            // Rotar la lista para que la más antigua esté al principio
            userNotifications.rotate(1);
            // Eliminar la primera (más antigua)
            userNotifications.remove(0);
        }
    }

    @Override
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Verificar si tenemos notificaciones en caché para este usuario
        CircularLinkedList<Notification> cachedNotifications = userNotificationsCache.get(userId);

        if (cachedNotifications != null && cachedNotifications.size() >= pageable.getPageSize()) {
            // Usar la caché si tiene suficientes notificaciones
            List<Notification> notificationsList = new ArrayList<>();

            // Convertir la lista circular a una lista normal
            for (Notification notification : cachedNotifications) {
                notificationsList.add(notification);
            }

            // Ordenar por fecha de creación (más reciente primero)
            notificationsList.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));

            // Aplicar paginación
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), notificationsList.size());

            if (start <= end) {
                List<Notification> pageContent = notificationsList.subList(start, end);

                // Convertir a respuestas
                List<NotificationResponse> responseList = new ArrayList<>();
                for (Notification notification : pageContent) {
                    responseList.add(convertToNotificationResponse(notification));
                }

                return new PageImpl<>(responseList, pageable, notificationsList.size());
            }
        }

        // Si no hay caché o no tiene suficientes notificaciones, usar la base de datos
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToNotificationResponse);
    }

    @Override
    public Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        return notificationRepository.findByUserAndRead(user, false, pageable)
                .map(this::convertToNotificationResponse);
    }

    @Override
    @Transactional
    public NotificationResponse markNotificationAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para acceder a esta notificación");
        }

        notification.markAsRead();
        Notification savedNotification = notificationRepository.save(notification);

        // Actualizar en la caché si existe
        updateNotificationInCache(userId, savedNotification);

        return convertToNotificationResponse(savedNotification);
    }

    /**
     * Actualiza una notificación en la caché
     */
    private void updateNotificationInCache(Long userId, Notification updatedNotification) {
        CircularLinkedList<Notification> userNotifications = userNotificationsCache.get(userId);

        if (userNotifications != null) {
            // Buscar la notificación en la lista
            for (int i = 0; i < userNotifications.size(); i++) {
                Notification cachedNotification = userNotifications.get(i);

                if (cachedNotification.getId().equals(updatedNotification.getId())) {
                    // Reemplazar con la versión actualizada
                    userNotifications.set(i, updatedNotification);
                    break;
                }
            }
        }
    }

    @Override
    @Transactional
    public int markAllNotificationsAsRead(Long userId) {
        int updatedCount = notificationRepository.markAllAsRead(userId, LocalDateTime.now());

        // Actualizar la caché
        if (updatedCount > 0) {
            CircularLinkedList<Notification> userNotifications = userNotificationsCache.get(userId);

            if (userNotifications != null) {
                // Recargar las notificaciones desde la base de datos
                List<Notification> updatedNotifications =
                    notificationRepository.findRecentByUser(userId, MAX_CACHED_NOTIFICATIONS);

                // Reemplazar la caché
                CircularLinkedList<Notification> newList = new CircularLinkedList<>();
                for (Notification notification : updatedNotifications) {
                    newList.add(notification);
                }

                userNotificationsCache.put(userId, newList);
            }
        }

        return updatedCount;
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta notificación");
        }

        notificationRepository.delete(notification);

        // Eliminar de la caché
        removeFromCache(userId, notificationId);
    }

    /**
     * Elimina una notificación de la caché
     */
    private void removeFromCache(Long userId, Long notificationId) {
        CircularLinkedList<Notification> userNotifications = userNotificationsCache.get(userId);

        if (userNotifications != null) {
            // Buscar y eliminar la notificación
            for (int i = 0; i < userNotifications.size(); i++) {
                if (userNotifications.get(i).getId().equals(notificationId)) {
                    userNotifications.remove(i);
                    break;
                }
            }
        }
    }

    @Override
    public long countUnreadNotifications(Long userId) {
        // Intentar contar desde la caché primero
        CircularLinkedList<Notification> userNotifications = userNotificationsCache.get(userId);

        if (userNotifications != null) {
            long count = 0;
            for (Notification notification : userNotifications) {
                if (!notification.isRead()) {
                    count++;
                }
            }
            return count;
        }

        // Si no hay caché, usar la base de datos
        return notificationRepository.countUnreadNotifications(userId);
    }

    private NotificationResponse convertToNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setCreatedAt(notification.getCreatedAt());
        response.setReadAt(notification.getReadAt());
        response.setRead(notification.isRead());
        response.setRelatedEntityId(notification.getRelatedEntityId());
        response.setRelatedEntityType(notification.getRelatedEntityType());
        return response;
    }

    @Override
    @Transactional
    public Notification createNotificationWithPreferences(Long userId, String title, String message, NotificationType type,
                                                        Long relatedEntityId, Notification.RelatedEntityType relatedEntityType) {
        // Verificar si el usuario debe recibir este tipo de notificación
        if (!preferenceService.shouldReceiveNotification(userId, type)) {
            logger.debug("Usuario {} ha desactivado las notificaciones de tipo {}", userId, type);
            return null;
        }

        // Crear la notificación
        Notification notification = createNotification(userId, title, message, type, relatedEntityId, relatedEntityType);

        // Enviar a través del canal configurado
        try {
            // Obtener el canal preferido
            NotificationChannel preferredChannel = preferenceService.getUserNotificationPreference(userId, type)
                    .getDeliveryChannel();

            if (preferredChannel != null && notification != null) {
                sendNotificationThroughChannel(notification.getId(), preferredChannel);
            }
        } catch (Exception e) {
            logger.error("Error al enviar notificación a través del canal preferido", e);
        }

        return notification;
    }

    @Override
    public boolean sendNotificationThroughChannel(Long notificationId, NotificationChannel channel) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada"));

        User user = notification.getUser();

        try {
            switch (channel) {
                case APP:
                    // Ya está guardada en la base de datos, no se necesita hacer nada más
                    return true;

                case EMAIL:
                    return sendEmailNotification(notification, user);

                case SMS:
                    // Implementación de envío de SMS (simulada)
                    logger.info("Simulando envío de SMS a {}: {}", user.getPhoneNumber(), notification.getTitle());
                    return true;

                case PUSH:
                    // Implementación de notificación push (simulada)
                    logger.info("Simulando envío de notificación push a {}: {}", user.getUsername(), notification.getTitle());
                    return true;

                default:
                    logger.warn("Canal de notificación no soportado: {}", channel);
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error al enviar notificación a través del canal " + channel, e);
            return false;
        }
    }

    /**
     * Envía una notificación por correo electrónico
     */
    private boolean sendEmailNotification(Notification notification, User user) {
        if (emailSender == null) {
            logger.warn("JavaMailSender no configurado, no se puede enviar correo electrónico");
            return false;
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            logger.warn("Usuario {} no tiene correo electrónico configurado", user.getId());
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(notification.getTitle());
            message.setText(notification.getMessage());

            emailSender.send(message);
            logger.info("Correo electrónico enviado a {}", user.getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Error al enviar correo electrónico", e);
            return false;
        }
    }

    @Override
    @Scheduled(fixedRate = 300000) // Cada 5 minutos
    @Transactional
    public int processPendingNotifications() {
        logger.info("Procesando notificaciones pendientes");

        // Obtener notificaciones pendientes (no leídas y no enviadas por otros canales)
        List<Notification> pendingNotifications = notificationRepository.findPendingNotifications();
        int processedCount = 0;

        for (Notification notification : pendingNotifications) {
            try {
                User user = notification.getUser();

                // Obtener el canal preferido
                NotificationChannel preferredChannel = preferenceService.getUserNotificationPreference(
                        user.getId(), notification.getType())
                        .getDeliveryChannel();

                if (preferredChannel != null && preferredChannel != NotificationChannel.APP) {
                    boolean sent = sendNotificationThroughChannel(notification.getId(), preferredChannel);

                    if (sent) {
                        processedCount++;
                    }
                }
            } catch (Exception e) {
                logger.error("Error al procesar notificación pendiente ID " + notification.getId(), e);
            }
        }

        logger.info("Procesadas {} notificaciones pendientes", processedCount);
        return processedCount;
    }

    @Override
    @Transactional
    public int cleanupOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        logger.info("Eliminando notificaciones anteriores a {}", cutoffDate);

        int deletedCount = notificationRepository.deleteOldNotifications(cutoffDate);

        // Actualizar la caché
        if (deletedCount > 0) {
            // Recargar la caché para todos los usuarios
            loadRecentNotifications();
        }

        logger.info("Se eliminaron {} notificaciones antiguas", deletedCount);
        return deletedCount;
    }

    /**
     * Método programado para limpiar notificaciones antiguas (más de 30 días)
     */
    @Scheduled(cron = "0 0 2 * * *") // Ejecutar a las 2 AM todos los días
    public void scheduledCleanupOldNotifications() {
        cleanupOldNotifications(30);
    }
}
