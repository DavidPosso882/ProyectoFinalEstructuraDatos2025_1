package org.example.controller;

import org.example.dto.request.WalletRelationRequest;
import org.example.dto.response.WalletRelationResponse;
import org.example.model.WalletRelationType;
import org.example.security.CurrentUser;
import org.example.security.UserPrincipal;
import org.example.service.WalletRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador para gestionar las relaciones entre monederos
 */
@RestController
@RequestMapping("/api/wallet-relations")
public class WalletRelationController {

    @Autowired
    private WalletRelationService walletRelationService;

    /**
     * Crea una nueva relación entre monederos
     * @param request Datos de la relación
     * @param currentUser Usuario autenticado
     * @return Relación creada
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletRelationResponse> createWalletRelation(
            @Valid @RequestBody WalletRelationRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.createWalletRelation(request, currentUser.getId()));
    }

    /**
     * Actualiza una relación existente
     * @param relationId ID de la relación
     * @param request Nuevos datos
     * @param currentUser Usuario autenticado
     * @return Relación actualizada
     */
    @PutMapping("/{relationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletRelationResponse> updateWalletRelation(
            @PathVariable Long relationId,
            @Valid @RequestBody WalletRelationRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.updateWalletRelation(relationId, request, currentUser.getId()));
    }

    /**
     * Elimina una relación
     * @param relationId ID de la relación
     * @param currentUser Usuario autenticado
     * @return Mensaje de confirmación
     */
    @DeleteMapping("/{relationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteWalletRelation(
            @PathVariable Long relationId,
            @CurrentUser UserPrincipal currentUser) {
        
        walletRelationService.deleteWalletRelation(relationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene una relación por su ID
     * @param relationId ID de la relación
     * @param currentUser Usuario autenticado
     * @return Relación
     */
    @GetMapping("/{relationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletRelationResponse> getWalletRelation(
            @PathVariable Long relationId,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.getWalletRelation(relationId, currentUser.getId()));
    }

    /**
     * Obtiene todas las relaciones del usuario
     * @param currentUser Usuario autenticado
     * @return Lista de relaciones
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WalletRelationResponse>> getUserWalletRelations(
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.getUserWalletRelations(currentUser.getId()));
    }

    /**
     * Obtiene todas las relaciones donde un monedero es el origen
     * @param walletId ID del monedero origen
     * @param currentUser Usuario autenticado
     * @return Lista de relaciones
     */
    @GetMapping("/outgoing/{walletId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WalletRelationResponse>> getWalletOutgoingRelations(
            @PathVariable Long walletId,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.getWalletOutgoingRelations(walletId, currentUser.getId()));
    }

    /**
     * Obtiene todas las relaciones donde un monedero es el destino
     * @param walletId ID del monedero destino
     * @param currentUser Usuario autenticado
     * @return Lista de relaciones
     */
    @GetMapping("/incoming/{walletId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WalletRelationResponse>> getWalletIncomingRelations(
            @PathVariable Long walletId,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.getWalletIncomingRelations(walletId, currentUser.getId()));
    }

    /**
     * Obtiene todas las relaciones de un tipo específico
     * @param relationType Tipo de relación
     * @param currentUser Usuario autenticado
     * @return Lista de relaciones
     */
    @GetMapping("/type/{relationType}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WalletRelationResponse>> getWalletRelationsByType(
            @PathVariable WalletRelationType relationType,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.getWalletRelationsByType(relationType, currentUser.getId()));
    }

    /**
     * Habilita o deshabilita las transferencias automáticas para una relación
     * @param relationId ID de la relación
     * @param enabled true para habilitar, false para deshabilitar
     * @param currentUser Usuario autenticado
     * @return Relación actualizada
     */
    @PatchMapping("/{relationId}/auto-transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletRelationResponse> setAutoTransferEnabled(
            @PathVariable Long relationId,
            @RequestParam boolean enabled,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.setAutoTransferEnabled(relationId, enabled, currentUser.getId()));
    }

    /**
     * Configura el porcentaje de transferencia automática para una relación
     * @param relationId ID de la relación
     * @param percentage Porcentaje (0-100)
     * @param currentUser Usuario autenticado
     * @return Relación actualizada
     */
    @PatchMapping("/{relationId}/percentage")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletRelationResponse> setAutoTransferPercentage(
            @PathVariable Long relationId,
            @RequestParam BigDecimal percentage,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.setAutoTransferPercentage(relationId, percentage, currentUser.getId()));
    }

    /**
     * Configura el umbral de transferencia automática para una relación
     * @param relationId ID de la relación
     * @param threshold Umbral mínimo para transferir
     * @param currentUser Usuario autenticado
     * @return Relación actualizada
     */
    @PatchMapping("/{relationId}/threshold")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletRelationResponse> setAutoTransferThreshold(
            @PathVariable Long relationId,
            @RequestParam BigDecimal threshold,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.setAutoTransferThreshold(relationId, threshold, currentUser.getId()));
    }

    /**
     * Verifica si existe un camino entre dos monederos
     * @param sourceWalletId ID del monedero origen
     * @param targetWalletId ID del monedero destino
     * @param currentUser Usuario autenticado
     * @return true si existe un camino, false en caso contrario
     */
    @GetMapping("/path-exists")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> existsPathBetweenWallets(
            @RequestParam Long sourceWalletId,
            @RequestParam Long targetWalletId,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.existsPathBetweenWallets(sourceWalletId, targetWalletId, currentUser.getId()));
    }

    /**
     * Encuentra el camino más corto entre dos monederos
     * @param sourceWalletId ID del monedero origen
     * @param targetWalletId ID del monedero destino
     * @param currentUser Usuario autenticado
     * @return Lista de relaciones que forman el camino, vacía si no existe
     */
    @GetMapping("/shortest-path")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WalletRelationResponse>> findShortestPath(
            @RequestParam Long sourceWalletId,
            @RequestParam Long targetWalletId,
            @CurrentUser UserPrincipal currentUser) {
        
        return ResponseEntity.ok(walletRelationService.findShortestPath(sourceWalletId, targetWalletId, currentUser.getId()));
    }
}
