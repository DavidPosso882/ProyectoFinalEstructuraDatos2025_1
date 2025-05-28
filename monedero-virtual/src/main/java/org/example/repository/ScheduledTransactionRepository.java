package org.example.repository;

import org.example.model.RecurrenceType;
import org.example.model.ScheduledTransaction;
import org.example.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {
    List<ScheduledTransaction> findByWallet(Wallet wallet);

    List<ScheduledTransaction> findByWalletAndExecuted(Wallet wallet, boolean executed);

    List<ScheduledTransaction> findByRecurrenceType(RecurrenceType recurrenceType);

    @Query("SELECT st FROM ScheduledTransaction st WHERE st.executed = false AND st.scheduledDate <= :now")
    List<ScheduledTransaction> findPendingScheduledTransactions(LocalDateTime now);

    @Query("SELECT st FROM ScheduledTransaction st WHERE st.executed = false AND st.scheduledDate BETWEEN :start AND :end")
    List<ScheduledTransaction> findUpcomingScheduledTransactions(LocalDateTime start, LocalDateTime end);

    @Query("SELECT st FROM ScheduledTransaction st WHERE st.wallet.user.id = :userId AND st.executed = false ORDER BY st.scheduledDate ASC")
    List<ScheduledTransaction> findPendingScheduledTransactionsByUser(Long userId);
}
