package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.dto.request.NotificationPreferenceRequest;
import org.example.dto.response.NotificationPreferenceResponse;
import org.example.model.NotificationChannel;
import org.example.model.NotificationPreference;
import org.example.model.NotificationType;
import org.example.model.User;
import org.example.repository.NotificationPreferenceRepository;
import org.example.repository.UserRepository;
import org.example.service.NotificationPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceServiceImpl.class);
    
    @Autowired
    private NotificationPreferenceRepository preferenceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public List<NotificationPreferenceResponse> getUserNotificationPreferences(Long userId) {
        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        return preferences.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public NotificationPreferenceResponse getUserNotificationPreference(Long userId, NotificationType notificationType) {
        NotificationPreference preference = preferenceRepository.findByUserIdAndNotificationType(userId, notificationType)
                .orElseThrow(() -> new EntityNotFoundException("Preferencia de notificación no encontrada"));
        
        return convertToResponse(preference);
    }
    
    @Override
    @Transactional
    public NotificationPreferenceResponse updateNotificationPreference(
            Long userId, NotificationType notificationType, NotificationPreferenceRequest request) {
        
        // Buscar la preferencia existente o crear una nueva
        NotificationPreference preference = preferenceRepository.findByUserIdAndNotificationType(userId, notificationType)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
                    
                    NotificationPreference newPreference = new NotificationPreference();
                    newPreference.setUser(user);
                    newPreference.setNotificationType(notificationType);
                    return newPreference;
                });
        
        // Actualizar los campos
        preference.setEnabled(request.isEnabled());
        preference.setDeliveryChannel(request.getDeliveryChannel());
        preference.setQuietHoursStart(request.getQuietHoursStart());
        preference.setQuietHoursEnd(request.getQuietHoursEnd());
        
        // Guardar los cambios
        NotificationPreference savedPreference = preferenceRepository.save(preference);
        
        return convertToResponse(savedPreference);
    }
    
    @Override
    @Transactional
    public List<NotificationPreferenceResponse> updateAllNotificationPreferences(
            Long userId, List<NotificationPreferenceRequest> requests) {
        
        List<NotificationPreferenceResponse> updatedPreferences = new ArrayList<>();
        
        for (NotificationPreferenceRequest request : requests) {
            NotificationPreferenceResponse updatedPreference = 
                updateNotificationPreference(userId, request.getNotificationType(), request);
            updatedPreferences.add(updatedPreference);
        }
        
        return updatedPreferences;
    }
    
    @Override
    @Transactional
    public NotificationPreferenceResponse setNotificationEnabled(
            Long userId, NotificationType notificationType, boolean enabled) {
        
        NotificationPreference preference = getOrCreatePreference(userId, notificationType);
        preference.setEnabled(enabled);
        
        NotificationPreference savedPreference = preferenceRepository.save(preference);
        
        return convertToResponse(savedPreference);
    }
    
    @Override
    @Transactional
    public NotificationPreferenceResponse setDeliveryChannel(
            Long userId, NotificationType notificationType, NotificationChannel channel) {
        
        NotificationPreference preference = getOrCreatePreference(userId, notificationType);
        preference.setDeliveryChannel(channel);
        
        NotificationPreference savedPreference = preferenceRepository.save(preference);
        
        return convertToResponse(savedPreference);
    }
    
    @Override
    @Transactional
    public NotificationPreferenceResponse setQuietHours(
            Long userId, NotificationType notificationType, Integer startHour, Integer endHour) {
        
        // Validar las horas
        if (startHour != null && (startHour < 0 || startHour > 23)) {
            throw new IllegalArgumentException("La hora de inicio debe estar entre 0 y 23");
        }
        
        if (endHour != null && (endHour < 0 || endHour > 23)) {
            throw new IllegalArgumentException("La hora de fin debe estar entre 0 y 23");
        }
        
        NotificationPreference preference = getOrCreatePreference(userId, notificationType);
        preference.setQuietHoursStart(startHour);
        preference.setQuietHoursEnd(endHour);
        
        NotificationPreference savedPreference = preferenceRepository.save(preference);
        
        return convertToResponse(savedPreference);
    }
    
    @Override
    public boolean shouldReceiveNotification(Long userId, NotificationType notificationType) {
        Optional<NotificationPreference> preferenceOpt = 
            preferenceRepository.findByUserIdAndNotificationType(userId, notificationType);
        
        if (preferenceOpt.isEmpty()) {
            // Si no hay preferencia configurada, usar el valor predeterminado (true)
            return true;
        }
        
        NotificationPreference preference = preferenceOpt.get();
        return preference.shouldDeliverNow();
    }
    
    @Override
    @Transactional
    public void initializeUserPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        
        // Verificar si ya tiene preferencias
        List<NotificationPreference> existingPreferences = preferenceRepository.findByUser(user);
        if (!existingPreferences.isEmpty()) {
            logger.info("El usuario {} ya tiene preferencias de notificación configuradas", userId);
            return;
        }
        
        // Crear preferencias predeterminadas para todos los tipos de notificación
        List<NotificationPreference> defaultPreferences = new ArrayList<>();
        
        for (NotificationType type : NotificationType.values()) {
            NotificationPreference preference = new NotificationPreference();
            preference.setUser(user);
            preference.setNotificationType(type);
            preference.setEnabled(true);
            preference.setDeliveryChannel(NotificationChannel.APP); // Por defecto, notificaciones en la app
            
            defaultPreferences.add(preference);
        }
        
        preferenceRepository.saveAll(defaultPreferences);
        
        logger.info("Inicializadas {} preferencias de notificación para el usuario {}", 
            defaultPreferences.size(), userId);
    }
    
    /**
     * Obtiene una preferencia existente o crea una nueva si no existe
     */
    private NotificationPreference getOrCreatePreference(Long userId, NotificationType notificationType) {
        return preferenceRepository.findByUserIdAndNotificationType(userId, notificationType)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
                    
                    NotificationPreference newPreference = new NotificationPreference();
                    newPreference.setUser(user);
                    newPreference.setNotificationType(notificationType);
                    newPreference.setEnabled(true);
                    return newPreference;
                });
    }
    
    /**
     * Convierte una entidad NotificationPreference a su DTO de respuesta
     */
    private NotificationPreferenceResponse convertToResponse(NotificationPreference preference) {
        NotificationPreferenceResponse response = new NotificationPreferenceResponse();
        response.setId(preference.getId());
        response.setUserId(preference.getUser().getId());
        response.setNotificationType(preference.getNotificationType());
        response.setNotificationTypeDescription(preference.getNotificationType().getDescription());
        response.setEnabled(preference.isEnabled());
        response.setDeliveryChannel(preference.getDeliveryChannel());
        
        if (preference.getDeliveryChannel() != null) {
            response.setDeliveryChannelName(preference.getDeliveryChannel().getName());
        }
        
        response.setQuietHoursStart(preference.getQuietHoursStart());
        response.setQuietHoursEnd(preference.getQuietHoursEnd());
        response.setCreatedAt(preference.getCreatedAt());
        response.setUpdatedAt(preference.getUpdatedAt());
        
        return response;
    }
}
