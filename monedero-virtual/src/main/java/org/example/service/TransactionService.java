package org.example.service;

import org.example.dto.request.TransactionRequest;
import org.example.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {

    /**
     * Procesa una transacción
     * @param transactionRequest Datos de la transacción
     * @param userId ID del usuario que realiza la transacción
     * @return Transacción procesada
     */
    TransactionResponse processTransaction(TransactionRequest transactionRequest, Long userId);

    /**
     * Obtiene las transacciones de un usuario
     * @param userId ID del usuario
     * @param pageable Paginación
     * @return Página de transacciones
     */
    Page<TransactionResponse> getUserTransactions(Long userId, Pageable pageable);

    /**
     * Obtiene una transacción por su ID
     * @param transactionId ID de la transacción
     * @param userId ID del usuario propietario
     * @return Transacción encontrada
     */
    TransactionResponse getTransactionById(Long transactionId, Long userId);

    /**
     * Obtiene las transacciones de un monedero
     * @param walletId ID del monedero
     * @param userId ID del usuario propietario
     * @param pageable Paginación
     * @return Página de transacciones
     */
    Page<TransactionResponse> getWalletTransactions(Long walletId, Long userId, Pageable pageable);

    /**
     * Revierte una transacción
     * @param transactionId ID de la transacción
     * @param userId ID del usuario propietario
     * @return Transacción revertida
     */
    TransactionResponse reverseTransaction(Long transactionId, Long userId);

    /**
     * Revierte la última transacción del usuario
     * @param userId ID del usuario
     * @return Transacción revertida o null si no hay transacciones para revertir
     */
    TransactionResponse reverseLastTransaction(Long userId);

    /**
     * Obtiene las últimas transacciones reversibles del usuario
     * @param userId ID del usuario
     * @param limit Número máximo de transacciones a devolver
     * @return Lista de transacciones reversibles
     */
    List<TransactionResponse> getReversibleTransactions(Long userId, int limit);

    /**
     * Verifica si una transacción es reversible
     * @param transactionId ID de la transacción
     * @param userId ID del usuario propietario
     * @return true si es reversible, false en caso contrario
     */
    boolean isTransactionReversible(Long transactionId, Long userId);
}
