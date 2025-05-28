package org.example.repository;

import org.example.model.Reward;
import org.example.model.RewardRedemption;
import org.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, Long> {
    
    /**
     * Busca un canje por su código
     * @param redemptionCode Código de canje
     * @return Canje encontrado
     */
    Optional<RewardRedemption> findByRedemptionCode(String redemptionCode);
    
    /**
     * Busca todos los canjes de un usuario
     * @param user Usuario
     * @return Lista de canjes
     */
    List<RewardRedemption> findByUser(User user);
    
    /**
     * Busca todos los canjes de un usuario paginados
     * @param user Usuario
     * @param pageable Paginación
     * @return Página de canjes
     */
    Page<RewardRedemption> findByUser(User user, Pageable pageable);
    
    /**
     * Busca todos los canjes de una recompensa
     * @param reward Recompensa
     * @return Lista de canjes
     */
    List<RewardRedemption> findByReward(Reward reward);
    
    /**
     * Busca todos los canjes de un usuario para una recompensa específica
     * @param user Usuario
     * @param reward Recompensa
     * @return Lista de canjes
     */
    List<RewardRedemption> findByUserAndReward(User user, Reward reward);
    
    /**
     * Cuenta cuántas veces un usuario ha canjeado una recompensa
     * @param userId ID del usuario
     * @param rewardId ID de la recompensa
     * @return Número de canjes
     */
    @Query("SELECT COUNT(r) FROM RewardRedemption r WHERE r.user.id = :userId AND r.reward.id = :rewardId")
    int countUserRedemptionsForReward(Long userId, Long rewardId);
    
    /**
     * Busca todos los canjes pendientes
     * @return Lista de canjes pendientes
     */
    List<RewardRedemption> findByStatus(RewardRedemption.RedemptionStatus status);
    
    /**
     * Busca todos los canjes realizados en un período de tiempo
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de canjes
     */
    List<RewardRedemption> findByRedemptionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Busca los últimos canjes de un usuario
     * @param userId ID del usuario
     * @param limit Número máximo de canjes a devolver
     * @return Lista de canjes
     */
    @Query("SELECT r FROM RewardRedemption r WHERE r.user.id = :userId ORDER BY r.redemptionDate DESC")
    List<RewardRedemption> findLatestUserRedemptions(Long userId, Pageable pageable);
}
