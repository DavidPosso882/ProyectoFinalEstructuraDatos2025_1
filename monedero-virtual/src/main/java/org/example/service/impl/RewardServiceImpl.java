package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.dto.request.PointsRedemptionRequest;
import org.example.dto.request.RewardCategoryRequest;
import org.example.dto.request.RewardRedemptionRequest;
import org.example.dto.request.RewardRequest;
import org.example.dto.response.PointsTransactionResponse;
import org.example.dto.response.RewardCategoryResponse;
import org.example.dto.response.RewardRedemptionResponse;
import org.example.dto.response.RewardResponse;
import org.example.model.*;
import org.example.repository.*;
import org.example.service.NotificationService;
import org.example.service.PointsAccountService;
import org.example.service.PointsService;
import org.example.service.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RewardServiceImpl implements RewardService {
    
    private static final Logger logger = LoggerFactory.getLogger(RewardServiceImpl.class);
    
    @Autowired
    private RewardCategoryRepository categoryRepository;
    
    @Autowired
    private RewardRepository rewardRepository;
    
    @Autowired
    private RewardRedemptionRepository redemptionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PointsAccountRepository pointsAccountRepository;
    
    @Autowired
    private PointsService pointsService;
    
    @Autowired
    private PointsAccountService pointsAccountService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    @Transactional
    public RewardCategoryResponse createCategory(RewardCategoryRequest request) {
        // Verificar si ya existe una categoría con el mismo nombre
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + request.getName());
        }
        
        RewardCategory category = new RewardCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconName(request.getIconName());
        category.setColorCode(request.getColorCode());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setActive(request.isActive());
        
        RewardCategory savedCategory = categoryRepository.save(category);
        logger.info("Categoría de recompensas creada: {}", savedCategory.getName());
        
        return convertToCategoryResponse(savedCategory);
    }
    
    @Override
    @Transactional
    public RewardCategoryResponse updateCategory(Long id, RewardCategoryRequest request) {
        RewardCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        
        // Verificar si el nombre ya está en uso por otra categoría
        if (!category.getName().equals(request.getName()) && 
                categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + request.getName());
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconName(request.getIconName());
        category.setColorCode(request.getColorCode());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setActive(request.isActive());
        
        RewardCategory updatedCategory = categoryRepository.save(category);
        logger.info("Categoría de recompensas actualizada: {}", updatedCategory.getName());
        
        return convertToCategoryResponse(updatedCategory);
    }
    
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        RewardCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        
        // Verificar si tiene recompensas asociadas
        if (!category.getRewards().isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar una categoría que tiene recompensas asociadas");
        }
        
        categoryRepository.delete(category);
        logger.info("Categoría de recompensas eliminada: {}", category.getName());
    }
    
    @Override
    public RewardCategoryResponse getCategoryById(Long id) {
        RewardCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        
        return convertToCategoryResponse(category);
    }
    
    @Override
    @Cacheable("rewardCategories")
    public List<RewardCategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable("activeRewardCategories")
    public List<RewardCategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"rewards", "activeRewards", "featuredRewards", "categoryRewards"}, allEntries = true)
    public RewardResponse createReward(RewardRequest request) {
        // Verificar si ya existe una recompensa con el mismo código
        if (rewardRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Ya existe una recompensa con el código: " + request.getCode());
        }
        
        // Verificar que la categoría existe
        RewardCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        }
        
        Reward reward = new Reward();
        reward.setCode(request.getCode());
        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        reward.setImageUrl(request.getImageUrl());
        reward.setPointsCost(request.getPointsCost());
        reward.setMonetaryValue(request.getMonetaryValue());
        reward.setActive(request.isActive());
        reward.setFeatured(request.isFeatured());
        reward.setStockQuantity(request.getStockQuantity());
        reward.setMaxPerUser(request.getMaxPerUser());
        reward.setStartDate(request.getStartDate());
        reward.setEndDate(request.getEndDate());
        reward.setCategory(category);
        
        Reward savedReward = rewardRepository.save(reward);
        logger.info("Recompensa creada: {}", savedReward.getName());
        
        return convertToRewardResponse(savedReward, null);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"rewards", "activeRewards", "featuredRewards", "categoryRewards"}, allEntries = true)
    public RewardResponse updateReward(Long id, RewardRequest request) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recompensa no encontrada"));
        
        // Verificar si el código ya está en uso por otra recompensa
        if (!reward.getCode().equals(request.getCode()) && 
                rewardRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Ya existe una recompensa con el código: " + request.getCode());
        }
        
        // Verificar que la categoría existe
        RewardCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        }
        
        reward.setCode(request.getCode());
        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        reward.setImageUrl(request.getImageUrl());
        reward.setPointsCost(request.getPointsCost());
        reward.setMonetaryValue(request.getMonetaryValue());
        reward.setActive(request.isActive());
        reward.setFeatured(request.isFeatured());
        reward.setStockQuantity(request.getStockQuantity());
        reward.setMaxPerUser(request.getMaxPerUser());
        reward.setStartDate(request.getStartDate());
        reward.setEndDate(request.getEndDate());
        reward.setCategory(category);
        
        Reward updatedReward = rewardRepository.save(reward);
        logger.info("Recompensa actualizada: {}", updatedReward.getName());
        
        return convertToRewardResponse(updatedReward, null);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"rewards", "activeRewards", "featuredRewards", "categoryRewards"}, allEntries = true)
    public void deleteReward(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recompensa no encontrada"));
        
        // Verificar si tiene canjes asociados
        if (!reward.getRedemptions().isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar una recompensa que tiene canjes asociados");
        }
        
        rewardRepository.delete(reward);
        logger.info("Recompensa eliminada: {}", reward.getName());
    }
    
    @Override
    public RewardResponse getRewardById(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recompensa no encontrada"));
        
        return convertToRewardResponse(reward, null);
    }
    
    @Override
    public RewardResponse getRewardByCode(String code) {
        Reward reward = rewardRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Recompensa no encontrada"));
        
        return convertToRewardResponse(reward, null);
    }
    
    @Override
    @Cacheable(value = "rewards", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<RewardResponse> getAllRewards(Pageable pageable) {
        return rewardRepository.findAll(pageable)
                .map(reward -> convertToRewardResponse(reward, null));
    }
    
    @Override
    @Cacheable(value = "activeRewards", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<RewardResponse> getActiveRewards(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return rewardRepository.findAvailableRewardsPaged(now, pageable)
                .map(reward -> convertToRewardResponse(reward, null));
    }
    
    @Override
    @Cacheable("featuredRewards")
    public List<RewardResponse> getFeaturedRewards() {
        return rewardRepository.findByActiveTrueAndFeaturedTrue().stream()
                .map(reward -> convertToRewardResponse(reward, null))
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "categoryRewards", key = "#categoryId")
    public List<RewardResponse> getRewardsByCategory(Long categoryId) {
        LocalDateTime now = LocalDateTime.now();
        return rewardRepository.findAvailableRewardsByCategory(categoryId, now).stream()
                .map(reward -> convertToRewardResponse(reward, null))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RewardResponse> getAvailableRewardsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        
        PointsAccount pointsAccount = pointsAccountRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta de puntos no encontrada"));
        
        Integer availablePoints = pointsAccount.getAvailablePoints();
        LocalDateTime now = LocalDateTime.now();
        
        List<Reward> availableRewards = rewardRepository.findRedeemableRewards(availablePoints, now);
        
        return availableRewards.stream()
                .map(reward -> {
                    // Verificar límite por usuario
                    boolean canRedeem = true;
                    if (reward.getMaxPerUser() != null) {
                        int userRedemptionCount = redemptionRepository.countUserRedemptionsForReward(userId, reward.getId());
                        canRedeem = userRedemptionCount < reward.getMaxPerUser();
                    }
                    
                    RewardResponse response = convertToRewardResponse(reward, userId);
                    response.setCanRedeem(canRedeem);
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public RewardRedemptionResponse redeemReward(RewardRedemptionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        
        Reward reward = rewardRepository.findByCode(request.getRewardCode())
                .orElseThrow(() -> new EntityNotFoundException("Recompensa no encontrada"));
        
        // Verificar si la recompensa está disponible
        if (!reward.isAvailable()) {
            throw new IllegalStateException("La recompensa no está disponible para canje");
        }
        
        // Verificar límite por usuario
        if (reward.getMaxPerUser() != null) {
            int userRedemptionCount = redemptionRepository.countUserRedemptionsForReward(userId, reward.getId());
            if (userRedemptionCount >= reward.getMaxPerUser()) {
                throw new IllegalStateException("Has alcanzado el límite de canjes para esta recompensa");
            }
        }
        
        // Verificar si el usuario tiene suficientes puntos
        PointsAccount pointsAccount = pointsAccountRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta de puntos no encontrada"));
        
        if (pointsAccount.getAvailablePoints() < reward.getPointsCost()) {
            throw new IllegalStateException("No tienes suficientes puntos para canjear esta recompensa");
        }
        
        // Canjear puntos
        PointsRedemptionRequest pointsRequest = new PointsRedemptionRequest();
        pointsRequest.setPoints(reward.getPointsCost());
        pointsRequest.setBenefitCode(reward.getCode());
        
        PointsTransactionResponse pointsTransaction = pointsService.redeemPoints(pointsRequest, userId);
        
        // Crear registro de canje
        RewardRedemption redemption = new RewardRedemption();
        redemption.setUser(user);
        redemption.setReward(reward);
        redemption.setPointsSpent(reward.getPointsCost());
        redemption.setNotes(request.getNotes());
        
        // Reducir stock
        reward.reduceStock();
        rewardRepository.save(reward);
        
        RewardRedemption savedRedemption = redemptionRepository.save(redemption);
        
        // Crear notificación
        notificationService.createNotification(
                userId,
                "Recompensa Canjeada",
                "Has canjeado " + reward.getPointsCost() + " puntos por " + reward.getName(),
                NotificationType.REWARD_REDEEMED,
                savedRedemption.getId(),
                Notification.RelatedEntityType.REWARD_REDEMPTION);
        
        logger.info("Recompensa canjeada: {} por el usuario {}", reward.getName(), user.getUsername());
        
        return convertToRedemptionResponse(savedRedemption);
    }
    
    @Override
    public RewardRedemptionResponse getRedemptionById(Long id) {
        RewardRedemption redemption = redemptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Canje no encontrado"));
        
        return convertToRedemptionResponse(redemption);
    }
    
    @Override
    public RewardRedemptionResponse getRedemptionByCode(String code) {
        RewardRedemption redemption = redemptionRepository.findByRedemptionCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Canje no encontrado"));
        
        return convertToRedemptionResponse(redemption);
    }
    
    @Override
    public Page<RewardRedemptionResponse> getUserRedemptions(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        
        return redemptionRepository.findByUser(user, pageable)
                .map(this::convertToRedemptionResponse);
    }
    
    @Override
    @Transactional
    public RewardRedemptionResponse completeRedemption(Long id) {
        RewardRedemption redemption = redemptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Canje no encontrado"));
        
        if (redemption.getStatus() != RewardRedemption.RedemptionStatus.PENDING && 
                redemption.getStatus() != RewardRedemption.RedemptionStatus.PROCESSING) {
            throw new IllegalStateException("No se puede completar un canje que no está pendiente o en proceso");
        }
        
        redemption.markAsCompleted();
        RewardRedemption savedRedemption = redemptionRepository.save(redemption);
        
        // Crear notificación
        notificationService.createNotification(
                redemption.getUser().getId(),
                "Recompensa Entregada",
                "Tu recompensa " + redemption.getReward().getName() + " ha sido entregada",
                NotificationType.REWARD_DELIVERED,
                savedRedemption.getId(),
                Notification.RelatedEntityType.REWARD_REDEMPTION);
        
        logger.info("Canje completado: {}", redemption.getRedemptionCode());
        
        return convertToRedemptionResponse(savedRedemption);
    }
    
    @Override
    @Transactional
    public RewardRedemptionResponse cancelRedemption(Long id, String reason) {
        RewardRedemption redemption = redemptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Canje no encontrado"));
        
        if (redemption.getStatus() == RewardRedemption.RedemptionStatus.COMPLETED || 
                redemption.getStatus() == RewardRedemption.RedemptionStatus.CANCELLED) {
            throw new IllegalStateException("No se puede cancelar un canje que ya está completado o cancelado");
        }
        
        redemption.markAsCancelled(reason);
        RewardRedemption savedRedemption = redemptionRepository.save(redemption);
        
        // Devolver puntos al usuario
        pointsAccountService.adjustPoints(
                redemption.getUser().getId(),
                redemption.getPointsSpent(),
                "Devolución de puntos por cancelación de canje: " + redemption.getReward().getName());
        
        // Incrementar stock
        Reward reward = redemption.getReward();
        if (reward.getStockQuantity() != null) {
            reward.setStockQuantity(reward.getStockQuantity() + 1);
            rewardRepository.save(reward);
        }
        
        // Crear notificación
        notificationService.createNotification(
                redemption.getUser().getId(),
                "Canje Cancelado",
                "Tu canje de " + redemption.getReward().getName() + " ha sido cancelado: " + reason,
                NotificationType.REWARD_CANCELLED,
                savedRedemption.getId(),
                Notification.RelatedEntityType.REWARD_REDEMPTION);
        
        logger.info("Canje cancelado: {}", redemption.getRedemptionCode());
        
        return convertToRedemptionResponse(savedRedemption);
    }
    
    @Override
    @Transactional
    public void initializeDefaultRewards() {
        // Verificar si ya existen categorías
        if (categoryRepository.count() > 0) {
            logger.info("Las categorías de recompensas ya están inicializadas");
            return;
        }
        
        logger.info("Inicializando categorías y recompensas predeterminadas");
        
        // Crear categorías
        RewardCategory discountsCategory = createDefaultCategory(
                "Descuentos", "Descuentos en productos y servicios", "discount", "#4CAF50", 1);
        
        RewardCategory giftCardsCategory = createDefaultCategory(
                "Tarjetas de Regalo", "Tarjetas de regalo para diferentes tiendas", "gift-card", "#2196F3", 2);
        
        RewardCategory cashbackCategory = createDefaultCategory(
                "Devolución de Dinero", "Devolución de dinero en tu monedero", "cashback", "#FF9800", 3);
        
        RewardCategory vipCategory = createDefaultCategory(
                "Beneficios VIP", "Beneficios exclusivos para usuarios VIP", "vip", "#9C27B0", 4);
        
        // Crear recompensas
        createDefaultReward("DISCOUNT_10", "Cupón de descuento 10%", 
                "Cupón de descuento del 10% en tu próxima compra", 
                "discount_10.jpg", 500, discountsCategory, 100, 3);
        
        createDefaultReward("DISCOUNT_20", "Cupón de descuento 20%", 
                "Cupón de descuento del 20% en tu próxima compra", 
                "discount_20.jpg", 1000, discountsCategory, 50, 2);
        
        createDefaultReward("GIFT_CARD_50", "Tarjeta de regalo de 50", 
                "Tarjeta de regalo por valor de 50 unidades", 
                "gift_card_50.jpg", 1500, giftCardsCategory, 30, 1);
        
        createDefaultReward("CASHBACK_100", "Devolución de 100 unidades", 
                "Recibe 100 unidades en tu monedero principal", 
                "cashback_100.jpg", 2000, cashbackCategory, null, 1);
        
        createDefaultReward("VIP_MONTH", "Mes de membresía VIP", 
                "Un mes de acceso a beneficios exclusivos VIP", 
                "vip_month.jpg", 3000, vipCategory, 10, 1);
        
        logger.info("Categorías y recompensas predeterminadas inicializadas correctamente");
    }
    
    /**
     * Crea una categoría predeterminada
     */
    private RewardCategory createDefaultCategory(String name, String description, String iconName, 
                                                String colorCode, Integer displayOrder) {
        RewardCategory category = new RewardCategory();
        category.setName(name);
        category.setDescription(description);
        category.setIconName(iconName);
        category.setColorCode(colorCode);
        category.setDisplayOrder(displayOrder);
        category.setActive(true);
        
        return categoryRepository.save(category);
    }
    
    /**
     * Crea una recompensa predeterminada
     */
    private Reward createDefaultReward(String code, String name, String description, String imageUrl, 
                                      Integer pointsCost, RewardCategory category, Integer stockQuantity, 
                                      Integer maxPerUser) {
        Reward reward = new Reward();
        reward.setCode(code);
        reward.setName(name);
        reward.setDescription(description);
        reward.setImageUrl(imageUrl);
        reward.setPointsCost(pointsCost);
        reward.setActive(true);
        reward.setCategory(category);
        reward.setStockQuantity(stockQuantity);
        reward.setMaxPerUser(maxPerUser);
        
        return rewardRepository.save(reward);
    }
    
    /**
     * Convierte una entidad RewardCategory a un DTO RewardCategoryResponse
     */
    private RewardCategoryResponse convertToCategoryResponse(RewardCategory category) {
        RewardCategoryResponse response = new RewardCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setIconName(category.getIconName());
        response.setColorCode(category.getColorCode());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setActive(category.isActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        // Contar recompensas
        response.setRewardCount(category.getRewards().size());
        
        return response;
    }
    
    /**
     * Convierte una entidad Reward a un DTO RewardResponse
     */
    private RewardResponse convertToRewardResponse(Reward reward, Long userId) {
        RewardResponse response = new RewardResponse();
        response.setId(reward.getId());
        response.setCode(reward.getCode());
        response.setName(reward.getName());
        response.setDescription(reward.getDescription());
        response.setImageUrl(reward.getImageUrl());
        response.setPointsCost(reward.getPointsCost());
        response.setMonetaryValue(reward.getMonetaryValue());
        response.setActive(reward.isActive());
        response.setFeatured(reward.isFeatured());
        response.setStockQuantity(reward.getStockQuantity());
        response.setMaxPerUser(reward.getMaxPerUser());
        response.setStartDate(reward.getStartDate());
        response.setEndDate(reward.getEndDate());
        response.setCreatedAt(reward.getCreatedAt());
        response.setUpdatedAt(reward.getUpdatedAt());
        
        // Establecer información de la categoría si existe
        if (reward.getCategory() != null) {
            response.setCategoryId(reward.getCategory().getId());
            response.setCategoryName(reward.getCategory().getName());
        }
        
        // Verificar disponibilidad
        response.setAvailable(reward.isAvailable());
        
        // Contar canjes
        response.setRedemptionCount(reward.getRedemptions().size());
        
        // Contar canjes del usuario si se proporciona el ID
        if (userId != null) {
            int userRedemptionCount = redemptionRepository.countUserRedemptionsForReward(userId, reward.getId());
            response.setUserRedemptionCount(userRedemptionCount);
            
            // Verificar si el usuario puede canjear esta recompensa
            boolean canRedeem = true;
            if (reward.getMaxPerUser() != null) {
                canRedeem = userRedemptionCount < reward.getMaxPerUser();
            }
            response.setCanRedeem(canRedeem && reward.isAvailable());
        }
        
        return response;
    }
    
    /**
     * Convierte una entidad RewardRedemption a un DTO RewardRedemptionResponse
     */
    private RewardRedemptionResponse convertToRedemptionResponse(RewardRedemption redemption) {
        RewardRedemptionResponse response = new RewardRedemptionResponse();
        response.setId(redemption.getId());
        response.setUserId(redemption.getUser().getId());
        response.setUsername(redemption.getUser().getUsername());
        response.setReward(convertToRewardResponse(redemption.getReward(), redemption.getUser().getId()));
        response.setPointsSpent(redemption.getPointsSpent());
        response.setRedemptionDate(redemption.getRedemptionDate());
        response.setRedemptionCode(redemption.getRedemptionCode());
        response.setStatus(redemption.getStatus());
        response.setStatusDescription(redemption.getStatus().getDescription());
        response.setDeliveryDate(redemption.getDeliveryDate());
        response.setNotes(redemption.getNotes());
        
        if (redemption.getPointsTransaction() != null) {
            response.setPointsTransactionId(redemption.getPointsTransaction().getId());
        }
        
        return response;
    }
}
