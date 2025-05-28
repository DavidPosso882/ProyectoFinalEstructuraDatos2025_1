package org.example.repository;

import org.example.model.Reward;
import org.example.model.RewardCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    
    /**
     * Busca una recompensa por su código
     * @param code Código de la recompensa
     * @return Recompensa encontrada
     */
    Optional<Reward> findByCode(String code);
    
    /**
     * Verifica si existe una recompensa con el código especificado
     * @param code Código de la recompensa
     * @return true si existe, false en caso contrario
     */
    boolean existsByCode(String code);
    
    /**
     * Busca todas las recompensas activas
     * @return Lista de recompensas activas
     */
    List<Reward> findByActiveTrue();
    
    /**
     * Busca todas las recompensas destacadas y activas
     * @return Lista de recompensas destacadas
     */
    List<Reward> findByActiveTrueAndFeaturedTrue();
    
    /**
     * Busca todas las recompensas de una categoría
     * @param category Categoría
     * @return Lista de recompensas
     */
    List<Reward> findByCategory(RewardCategory category);
    
    /**
     * Busca todas las recompensas activas de una categoría
     * @param category Categoría
     * @return Lista de recompensas activas
     */
    List<Reward> findByCategoryAndActiveTrue(RewardCategory category);
    
    /**
     * Busca todas las recompensas disponibles (activas y con stock)
     * @return Lista de recompensas disponibles
     */
    @Query("SELECT r FROM Reward r WHERE r.active = true AND (r.stockQuantity IS NULL OR r.stockQuantity > 0) " +
           "AND (r.startDate IS NULL OR r.startDate <= :now) " +
           "AND (r.endDate IS NULL OR r.endDate >= :now)")
    List<Reward> findAvailableRewards(LocalDateTime now);
    
    /**
     * Busca todas las recompensas disponibles (activas y con stock) paginadas
     * @param now Fecha actual
     * @param pageable Paginación
     * @return Página de recompensas disponibles
     */
    @Query("SELECT r FROM Reward r WHERE r.active = true AND (r.stockQuantity IS NULL OR r.stockQuantity > 0) " +
           "AND (r.startDate IS NULL OR r.startDate <= :now) " +
           "AND (r.endDate IS NULL OR r.endDate >= :now)")
    Page<Reward> findAvailableRewardsPaged(LocalDateTime now, Pageable pageable);
    
    /**
     * Busca todas las recompensas disponibles (activas y con stock) de una categoría
     * @param categoryId ID de la categoría
     * @param now Fecha actual
     * @return Lista de recompensas disponibles
     */
    @Query("SELECT r FROM Reward r WHERE r.category.id = :categoryId AND r.active = true " +
           "AND (r.stockQuantity IS NULL OR r.stockQuantity > 0) " +
           "AND (r.startDate IS NULL OR r.startDate <= :now) " +
           "AND (r.endDate IS NULL OR r.endDate >= :now)")
    List<Reward> findAvailableRewardsByCategory(Long categoryId, LocalDateTime now);
    
    /**
     * Busca todas las recompensas que un usuario puede canjear con sus puntos disponibles
     * @param availablePoints Puntos disponibles
     * @param now Fecha actual
     * @return Lista de recompensas canjeables
     */
    @Query("SELECT r FROM Reward r WHERE r.pointsCost <= :availablePoints AND r.active = true " +
           "AND (r.stockQuantity IS NULL OR r.stockQuantity > 0) " +
           "AND (r.startDate IS NULL OR r.startDate <= :now) " +
           "AND (r.endDate IS NULL OR r.endDate >= :now)")
    List<Reward> findRedeemableRewards(Integer availablePoints, LocalDateTime now);
}
