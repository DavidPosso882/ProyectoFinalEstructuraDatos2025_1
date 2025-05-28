package org.example.service;

import org.example.dto.request.RewardCategoryRequest;
import org.example.dto.request.RewardRedemptionRequest;
import org.example.dto.request.RewardRequest;
import org.example.dto.response.RewardCategoryResponse;
import org.example.dto.response.RewardRedemptionResponse;
import org.example.dto.response.RewardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RewardService {
    
    /**
     * Crea una nueva categoría de recompensas
     * @param request Datos de la categoría
     * @return Categoría creada
     */
    RewardCategoryResponse createCategory(RewardCategoryRequest request);
    
    /**
     * Actualiza una categoría existente
     * @param id ID de la categoría
     * @param request Datos actualizados
     * @return Categoría actualizada
     */
    RewardCategoryResponse updateCategory(Long id, RewardCategoryRequest request);
    
    /**
     * Elimina una categoría
     * @param id ID de la categoría
     */
    void deleteCategory(Long id);
    
    /**
     * Obtiene una categoría por su ID
     * @param id ID de la categoría
     * @return Categoría encontrada
     */
    RewardCategoryResponse getCategoryById(Long id);
    
    /**
     * Obtiene todas las categorías
     * @return Lista de categorías
     */
    List<RewardCategoryResponse> getAllCategories();
    
    /**
     * Obtiene todas las categorías activas
     * @return Lista de categorías activas
     */
    List<RewardCategoryResponse> getActiveCategories();
    
    /**
     * Crea una nueva recompensa
     * @param request Datos de la recompensa
     * @return Recompensa creada
     */
    RewardResponse createReward(RewardRequest request);
    
    /**
     * Actualiza una recompensa existente
     * @param id ID de la recompensa
     * @param request Datos actualizados
     * @return Recompensa actualizada
     */
    RewardResponse updateReward(Long id, RewardRequest request);
    
    /**
     * Elimina una recompensa
     * @param id ID de la recompensa
     */
    void deleteReward(Long id);
    
    /**
     * Obtiene una recompensa por su ID
     * @param id ID de la recompensa
     * @return Recompensa encontrada
     */
    RewardResponse getRewardById(Long id);
    
    /**
     * Obtiene una recompensa por su código
     * @param code Código de la recompensa
     * @return Recompensa encontrada
     */
    RewardResponse getRewardByCode(String code);
    
    /**
     * Obtiene todas las recompensas
     * @param pageable Paginación
     * @return Página de recompensas
     */
    Page<RewardResponse> getAllRewards(Pageable pageable);
    
    /**
     * Obtiene todas las recompensas activas
     * @param pageable Paginación
     * @return Página de recompensas activas
     */
    Page<RewardResponse> getActiveRewards(Pageable pageable);
    
    /**
     * Obtiene todas las recompensas destacadas
     * @return Lista de recompensas destacadas
     */
    List<RewardResponse> getFeaturedRewards();
    
    /**
     * Obtiene todas las recompensas de una categoría
     * @param categoryId ID de la categoría
     * @return Lista de recompensas
     */
    List<RewardResponse> getRewardsByCategory(Long categoryId);
    
    /**
     * Obtiene todas las recompensas disponibles para un usuario
     * @param userId ID del usuario
     * @return Lista de recompensas disponibles
     */
    List<RewardResponse> getAvailableRewardsForUser(Long userId);
    
    /**
     * Canjea una recompensa
     * @param request Datos del canje
     * @param userId ID del usuario
     * @return Canje creado
     */
    RewardRedemptionResponse redeemReward(RewardRedemptionRequest request, Long userId);
    
    /**
     * Obtiene un canje por su ID
     * @param id ID del canje
     * @return Canje encontrado
     */
    RewardRedemptionResponse getRedemptionById(Long id);
    
    /**
     * Obtiene un canje por su código
     * @param code Código del canje
     * @return Canje encontrado
     */
    RewardRedemptionResponse getRedemptionByCode(String code);
    
    /**
     * Obtiene todos los canjes de un usuario
     * @param userId ID del usuario
     * @param pageable Paginación
     * @return Página de canjes
     */
    Page<RewardRedemptionResponse> getUserRedemptions(Long userId, Pageable pageable);
    
    /**
     * Marca un canje como completado
     * @param id ID del canje
     * @return Canje actualizado
     */
    RewardRedemptionResponse completeRedemption(Long id);
    
    /**
     * Cancela un canje
     * @param id ID del canje
     * @param reason Motivo de la cancelación
     * @return Canje actualizado
     */
    RewardRedemptionResponse cancelRedemption(Long id, String reason);
    
    /**
     * Inicializa las categorías y recompensas predeterminadas
     */
    void initializeDefaultRewards();
}
