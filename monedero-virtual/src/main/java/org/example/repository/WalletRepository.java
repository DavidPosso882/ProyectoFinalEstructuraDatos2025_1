package org.example.repository;

import org.example.model.User;
import org.example.model.Wallet;
import org.example.model.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUser(User user);
    
    List<Wallet> findByUserAndWalletType(User user, WalletType walletType);
    
    Optional<Wallet> findByUserAndName(User user, String name);
    
    @Query("SELECT w FROM Wallet w WHERE w.balance < :minBalance")
    List<Wallet> findWalletsWithLowBalance(BigDecimal minBalance);
    
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId ORDER BY w.balance DESC")
    List<Wallet> findUserWalletsOrderedByBalanceDesc(Long userId);
    
    // Método para encontrar monederos por ID de usuario
    List<Wallet> findByUserId(Long userId);
}
