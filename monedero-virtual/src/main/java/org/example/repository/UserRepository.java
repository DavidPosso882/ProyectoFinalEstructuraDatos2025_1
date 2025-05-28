package org.example.repository;

import org.example.model.User;
import org.example.model.UserRank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRank(UserRank rank);

    Page<User> findByRank(UserRank rank, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.wallets w WHERE w.balance < :minBalance")
    List<User> findUsersWithLowBalance(java.math.BigDecimal minBalance);

    /**
     * Encuentra usuarios con actividad reciente (notificaciones o transacciones)
     * @param since Fecha desde la que buscar actividad
     * @return Lista de usuarios con actividad reciente
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN u.notifications n " +
           "LEFT JOIN u.wallets w " +
           "LEFT JOIN Transaction t ON (t.sourceWallet = w OR t.targetWallet = w) " +
           "WHERE n.createdAt > :since OR t.transactionDate > :since")
    List<User> findUsersWithRecentActivity(@Param("since") LocalDateTime since);

    /**
     * Verifica si un usuario ha tenido actividad reciente
     * @param userId ID del usuario
     * @param since Fecha desde la que buscar actividad
     * @return true si el usuario ha tenido actividad reciente, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
           "LEFT JOIN u.notifications n " +
           "LEFT JOIN u.wallets w " +
           "LEFT JOIN Transaction t ON (t.sourceWallet = w OR t.targetWallet = w) " +
           "WHERE u.id = :userId AND (n.createdAt > :since OR t.transactionDate > :since)")
    boolean hasUserRecentActivity(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}