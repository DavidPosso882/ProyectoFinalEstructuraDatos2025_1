package org.example.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import jakarta.annotation.PostConstruct;
import org.example.model.Notification;
import org.example.model.NotificationType;
import org.example.repository.NotificationRepository;
import org.example.repository.UserRepository;
import org.example.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;

@Service
@Primary
public class HazelcastNotificationService extends NotificationServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(HazelcastNotificationService.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @PostConstruct
    public void init() {
        // Suscribirse al topic de notificaciones
        ITopic<NotificationMessage> topic = hazelcastInstance.getTopic("notifications");
        topic.addMessageListener(message -> {
            NotificationMessage notificationMsg = message.getMessageObject();
            logger.info("Recibida notificación para usuario {}: {}",
                    notificationMsg.getUserId(), notificationMsg.getTitle());

            // Aquí se podría implementar la lógica para enviar la notificación
            // al cliente a través de WebSockets, por ejemplo
        });

        logger.info("Servicio de notificaciones Hazelcast inicializado");
    }

    @Override
    @Transactional
    public Notification createNotification(Long userId, String title, String message, NotificationType type,
                                          Long relatedEntityId, Notification.RelatedEntityType relatedEntityType) {
        // Crear la notificación en la base de datos usando la implementación original
        Notification notification = super.createNotification(userId, title, message, type,
                                                           relatedEntityId, relatedEntityType);

        // Publicar la notificación en Hazelcast para procesamiento en tiempo real
        ITopic<NotificationMessage> topic = hazelcastInstance.getTopic("notifications");
        topic.publish(new NotificationMessage(userId, title, message, type.name()));

        return notification;
    }

    /**
     * Clase interna para representar un mensaje de notificación serializable
     */
    public static class NotificationMessage implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String title;
        private String message;
        private String type;
        private LocalDateTime timestamp;

        public NotificationMessage(Long userId, String title, String message, String type) {
            this.userId = userId;
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = LocalDateTime.now();
        }

        public Long getUserId() {
            return userId;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public String getType() {
            return type;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
