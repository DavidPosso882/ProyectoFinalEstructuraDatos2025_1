package org.example.service;

import org.example.dto.request.WalletRelationRequest;
import org.example.dto.response.WalletRelationResponse;
import org.example.model.WalletRelationType;

import java.math.BigDecimal;
import java.util.List;

public interface WalletRelationService {
    
    /**
     * Crea una nueva relación entre monederos
     * @param request Datos de la relación
     * @param userId ID del usuario propietario
     * @return Relación creada
     */
    WalletRelationResponse createWalletRelation(WalletRelationRequest request, Long userId);
    
    /**
     * Actualiza una relación existente
     * @param relationId ID de la relación
     * @param request Nuevos datos
     * @param userId ID del usuario propietario
     * @return Relación actualizada
     */
    WalletRelationResponse updateWalletRelation(Long relationId, WalletRelationRequest request, Long userId);
    
    /**
     * Elimina una relación
     * @param relationId ID de la relación
     * @param userId ID del usuario propietario
     */
    void deleteWalletRelation(Long relationId, Long userId);
    
    /**
     * Obtiene una relación por su ID
     * @param relationId ID de la relación
     * @param userId ID del usuario propietario
     * @return Relación
     */
    WalletRelationResponse getWalletRelation(Long relationId, Long userId);
    
    /**
     * Obtiene todas las relaciones de un usuario
     * @param userId ID del usuario
     * @return Lista de relaciones
     */
    List<WalletRelationResponse> getUserWalletRelations(Long userId);
    
    /**
     * Obtiene todas las relaciones donde un monedero es el origen
     * @param walletId ID del monedero origen
     * @param userId ID del usuario propietario
     * @return Lista de relaciones
     */
    List<WalletRelationResponse> getWalletOutgoingRelations(Long walletId, Long userId);
    
    /**
     * Obtiene todas las relaciones donde un monedero es el destino
     * @param walletId ID del monedero destino
     * @param userId ID del usuario propietario
     * @return Lista de relaciones
     */
    List<WalletRelationResponse> getWalletIncomingRelations(Long walletId, Long userId);
    
    /**
     * Obtiene todas las relaciones de un tipo específico
     * @param relationType Tipo de relación
     * @param userId ID del usuario propietario
     * @return Lista de relaciones
     */
    List<WalletRelationResponse> getWalletRelationsByType(WalletRelationType relationType, Long userId);
    
    /**
     * Habilita o deshabilita las transferencias automáticas para una relación
     * @param relationId ID de la relación
     * @param enabled true para habilitar, false para deshabilitar
     * @param userId ID del usuario propietario
     * @return Relación actualizada
     */
    WalletRelationResponse setAutoTransferEnabled(Long relationId, boolean enabled, Long userId);
    
    /**
     * Configura el porcentaje de transferencia automática para una relación
     * @param relationId ID de la relación
     * @param percentage Porcentaje (0-100)
     * @param userId ID del usuario propietario
     * @return Relación actualizada
     */
    WalletRelationResponse setAutoTransferPercentage(Long relationId, BigDecimal percentage, Long userId);
    
    /**
     * Configura el umbral de transferencia automática para una relación
     * @param relationId ID de la relación
     * @param threshold Umbral mínimo para transferir
     * @param userId ID del usuario propietario
     * @return Relación actualizada
     */
    WalletRelationResponse setAutoTransferThreshold(Long relationId, BigDecimal threshold, Long userId);
    
    /**
     * Procesa transferencias automáticas basadas en un depósito
     * @param walletId ID del monedero donde se realizó el depósito
     * @param amount Monto depositado
     * @param userId ID del usuario propietario
     * @return Lista de transacciones generadas
     */
    List<WalletRelationResponse> processAutoTransfers(Long walletId, BigDecimal amount, Long userId);
    
    /**
     * Verifica si existe un camino entre dos monederos
     * @param sourceWalletId ID del monedero origen
     * @param targetWalletId ID del monedero destino
     * @param userId ID del usuario propietario
     * @return true si existe un camino, false en caso contrario
     */
    boolean existsPathBetweenWallets(Long sourceWalletId, Long targetWalletId, Long userId);
    
    /**
     * Encuentra el camino más corto entre dos monederos
     * @param sourceWalletId ID del monedero origen
     * @param targetWalletId ID del monedero destino
     * @param userId ID del usuario propietario
     * @return Lista de relaciones que forman el camino, vacía si no existe
     */
    List<WalletRelationResponse> findShortestPath(Long sourceWalletId, Long targetWalletId, Long userId);
}
