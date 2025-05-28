package org.example.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Servicio para gestionar transacciones distribuidas con Hazelcast
 */
@Service
public class HazelcastTransactionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(HazelcastTransactionManager.class);
    
    @Autowired
    private HazelcastInstance hazelcastInstance;
    
    /**
     * Ejecuta una operación dentro de una transacción Hazelcast
     * @param operation La operación a ejecutar
     * @param <T> El tipo de retorno de la operación
     * @return El resultado de la operación
     */
    public <T> T executeInTransaction(Supplier<T> operation) {
        TransactionOptions options = TransactionOptions.getDefault()
                .setTimeout(10, TimeUnit.SECONDS);
        
        TransactionContext context = hazelcastInstance.newTransactionContext(options);
        context.beginTransaction();
        
        try {
            T result = operation.get();
            context.commitTransaction();
            return result;
        } catch (Exception e) {
            logger.error("Error en transacción Hazelcast: {}", e.getMessage());
            context.rollbackTransaction();
            throw e;
        }
    }
    
    /**
     * Actualiza el saldo de un monedero de forma atómica
     * @param walletId ID del monedero
     * @param amount Cantidad a añadir (positiva) o restar (negativa)
     * @return El nuevo saldo
     */
    public BigDecimal updateWalletBalance(Long walletId, BigDecimal amount) {
        return executeInTransaction(() -> {
            TransactionContext context = hazelcastInstance.newTransactionContext();
            TransactionalMap<String, BigDecimal> walletBalances = 
                    context.getMap("walletBalances");
            
            String key = "wallet:" + walletId;
            BigDecimal currentBalance = walletBalances.getForUpdate(key);
            
            if (currentBalance == null) {
                // Si no está en caché, obtenerlo de la base de datos
                // (esto sería implementado por el servicio que use este método)
                return amount;
            }
            
            BigDecimal newBalance = currentBalance.add(amount);
            walletBalances.put(key, newBalance);
            
            return newBalance;
        });
    }
    
    /**
     * Verifica si un monedero tiene saldo suficiente
     * @param walletId ID del monedero
     * @param amount Cantidad a verificar
     * @return true si hay saldo suficiente, false en caso contrario
     */
    public boolean hasWalletSufficientBalance(Long walletId, BigDecimal amount) {
        IMap<String, BigDecimal> walletBalances = hazelcastInstance.getMap("walletBalances");
        String key = "wallet:" + walletId;
        BigDecimal balance = walletBalances.get(key);
        
        if (balance == null) {
            // Si no está en caché, devolver false y dejar que el servicio
            // que use este método verifique en la base de datos
            return false;
        }
        
        return balance.compareTo(amount) >= 0;
    }
    
    /**
     * Actualiza los puntos de un usuario de forma atómica
     * @param userId ID del usuario
     * @param points Cantidad de puntos a añadir (positiva) o restar (negativa)
     * @return Los nuevos puntos
     */
    public Integer updateUserPoints(Long userId, Integer points) {
        return executeInTransaction(() -> {
            TransactionContext context = hazelcastInstance.newTransactionContext();
            TransactionalMap<String, Integer> userPoints = 
                    context.getMap("userPoints");
            
            String key = "user:" + userId;
            Integer currentPoints = userPoints.getForUpdate(key);
            
            if (currentPoints == null) {
                // Si no está en caché, obtenerlo de la base de datos
                return points;
            }
            
            Integer newPoints = currentPoints + points;
            userPoints.put(key, newPoints);
            
            return newPoints;
        });
    }
}
