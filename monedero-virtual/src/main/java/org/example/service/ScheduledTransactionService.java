package org.example.service;

import org.example.dto.request.ScheduledTransactionRequest;
import org.example.dto.response.ScheduledTransactionResponse;

import java.util.List;

public interface ScheduledTransactionService {
    
    /**
     * Crea una transacción programada
     * @param request Datos de la transacción programada
     * @param userId ID del usuario
     * @return Transacción programada creada
     */
    ScheduledTransactionResponse createScheduledTransaction(ScheduledTransactionRequest request, Long userId);
    
    /**
     * Obtiene las transacciones programadas de un usuario
     * @param userId ID del usuario
     * @return Lista de transacciones programadas
     */
    List<ScheduledTransactionResponse> getUserScheduledTransactions(Long userId);
    
    /**
     * Obtiene una transacción programada por su ID
     * @param id ID de la transacción programada
     * @param userId ID del usuario propietario
     * @return Transacción programada encontrada
     */
    ScheduledTransactionResponse getScheduledTransactionById(Long id, Long userId);
    
    /**
     * Cancela una transacción programada
     * @param id ID de la transacción programada
     * @param userId ID del usuario propietario
     */
    void cancelScheduledTransaction(Long id, Long userId);
    
    /**
     * Ejecuta las transacciones programadas pendientes
     * Este método debería ser llamado por un programador de tareas
     */
    void executePendingScheduledTransactions();
}
