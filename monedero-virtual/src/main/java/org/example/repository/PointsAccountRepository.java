package org.example.repository;

import org.example.model.PointsAccount;
import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointsAccountRepository extends JpaRepository<PointsAccount, Long> {
    Optional<PointsAccount> findByUser(User user);
    
    @Query("SELECT pa FROM PointsAccount pa ORDER BY pa.totalPoints DESC")
    List<PointsAccount> findTopPointsAccounts();
    
    @Query("SELECT pa FROM PointsAccount pa WHERE pa.availablePoints > :minPoints ORDER BY pa.availablePoints DESC")
    List<PointsAccount> findAccountsWithMinimumAvailablePoints(Integer minPoints);
    
    @Query("SELECT SUM(pa.totalPoints) FROM PointsAccount pa")
    Integer getTotalPointsInSystem();
}
