package org.example.service;

import org.example.dto.request.PointsRedemptionRequest;
import org.example.dto.response.PointsAccountResponse;
import org.example.dto.response.PointsTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface PointsService {
    
    /**
     * Obtiene la cuenta de puntos de un usuario
     * @param userId ID del usuario
     * @return Cuenta de puntos
     */
    PointsAccountResponse getUserPointsAccount(Long userId);
    
    /**
     * Obtiene las transacciones de puntos de un usuario
     * @param userId ID del usuario
     * @param pageable Paginación
     * @return Página de transacciones de puntos
     */
    Page<PointsTransactionResponse> getUserPointsTransactions(Long userId, Pageable pageable);
    
    /**
     * Canjea puntos por un beneficio
     * @param request Datos del canje
     * @param userId ID del usuario
     * @return Transacción de puntos
     */
    PointsTransactionResponse redeemPoints(PointsRedemptionRequest request, Long userId);
    
    /**
     * Obtiene los beneficios disponibles para un usuario
     * @param userId ID del usuario
     * @return Mapa de beneficios disponibles
     */
    Map<String, Object> getAvailableBenefits(Long userId);
}
