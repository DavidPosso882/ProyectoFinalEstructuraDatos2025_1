package org.example.service.impl;

import jakarta.annotation.PostConstruct;
import org.example.datastructure.CustomStack;
import org.example.model.Transaction;
import org.example.model.TransactionStatus;
import org.example.model.TransactionType;
import org.example.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Gestor de historial de transacciones utilizando pilas para permitir reversiones
 */
@Component
public class TransactionHistoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionHistoryManager.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    // Mapa de pilas de transacciones por usuario
    private Map<Long, CustomStack<Transaction>> userTransactionStacks;
    
    // Tamaño máximo de la pila por usuario
    private static final int MAX_STACK_SIZE = 50;
    
    @PostConstruct
    public void init() {
        userTransactionStacks = new HashMap<>();
    }
    
    /**
     * Registra una transacción en la pila del usuario
     * @param transaction Transacción a registrar
     */
    public void pushTransaction(Transaction transaction) {
        if (transaction == null || transaction.getStatus() != TransactionStatus.COMPLETED) {
            return;
        }
        
        // Determinar el ID del usuario
        Long userId = getUserIdFromTransaction(transaction);
        if (userId == null) {
            return;
        }
        
        // Obtener o crear la pila para el usuario
        CustomStack<Transaction> userStack = userTransactionStacks.computeIfAbsent(
            userId, k -> new CustomStack<>());
        
        // Añadir la transacción a la pila
        userStack.push(transaction);
        
        // Limitar el tamaño de la pila
        if (userStack.size() > MAX_STACK_SIZE) {
            userStack.removeLast();
        }
        
        logger.debug("Transacción {} añadida a la pila del usuario {}", 
            transaction.getId(), userId);
    }
    
    /**
     * Obtiene la última transacción del usuario sin eliminarla de la pila
     * @param userId ID del usuario
     * @return Última transacción o null si no hay ninguna
     */
    public Transaction peekLastTransaction(Long userId) {
        CustomStack<Transaction> userStack = userTransactionStacks.get(userId);
        if (userStack == null || userStack.isEmpty()) {
            return null;
        }
        
        return userStack.peek();
    }
    
    /**
     * Obtiene y elimina la última transacción del usuario
     * @param userId ID del usuario
     * @return Última transacción o null si no hay ninguna
     */
    public Transaction popLastTransaction(Long userId) {
        CustomStack<Transaction> userStack = userTransactionStacks.get(userId);
        if (userStack == null || userStack.isEmpty()) {
            return null;
        }
        
        Transaction transaction = userStack.pop();
        logger.debug("Transacción {} extraída de la pila del usuario {}", 
            transaction.getId(), userId);
        
        return transaction;
    }
    
    /**
     * Verifica si una transacción es reversible
     * @param transaction Transacción a verificar
     * @return true si es reversible, false en caso contrario
     */
    public boolean isTransactionReversible(Transaction transaction) {
        if (transaction == null || transaction.isReversed() || 
            transaction.getStatus() != TransactionStatus.COMPLETED) {
            return false;
        }
        
        // Verificar el tipo de transacción
        TransactionType type = transaction.getType();
        return type == TransactionType.DEPOSIT || 
               type == TransactionType.WITHDRAWAL || 
               type == TransactionType.TRANSFER;
    }
    
    /**
     * Obtiene una transacción por su ID y verifica si es reversible
     * @param transactionId ID de la transacción
     * @return Transacción si existe y es reversible, vacío en caso contrario
     */
    public Optional<Transaction> getReversibleTransaction(Long transactionId) {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
        
        if (optionalTransaction.isPresent() && isTransactionReversible(optionalTransaction.get())) {
            return optionalTransaction;
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtiene el ID del usuario asociado a una transacción
     * @param transaction Transacción
     * @return ID del usuario o null si no se puede determinar
     */
    private Long getUserIdFromTransaction(Transaction transaction) {
        if (transaction.getSourceWallet() != null) {
            return transaction.getSourceWallet().getUser().getId();
        } else if (transaction.getTargetWallet() != null) {
            return transaction.getTargetWallet().getUser().getId();
        }
        return null;
    }
    
    /**
     * Limpia las pilas de transacciones antiguas
     */
    public void cleanupOldTransactions() {
        // Implementación simple: eliminar pilas con más de 100 elementos
        userTransactionStacks.entrySet().removeIf(entry -> entry.getValue().size() > 100);
    }
}
