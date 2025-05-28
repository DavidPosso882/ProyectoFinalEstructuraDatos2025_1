package org.example.repository;

import org.example.model.Wallet;
import org.example.model.WalletRelation;
import org.example.model.WalletRelationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRelationRepository extends JpaRepository<WalletRelation, Long> {
    
    /**
     * Encuentra todas las relaciones donde un monedero es el origen
     * @param sourceWallet Monedero origen
     * @return Lista de relaciones
     */
    List<WalletRelation> findBySourceWallet(Wallet sourceWallet);
    
    /**
     * Encuentra todas las relaciones donde un monedero es el destino
     * @param targetWallet Monedero destino
     * @return Lista de relaciones
     */
    List<WalletRelation> findByTargetWallet(Wallet targetWallet);
    
    /**
     * Encuentra todas las relaciones de un tipo específico donde un monedero es el origen
     * @param sourceWallet Monedero origen
     * @param relationType Tipo de relación
     * @return Lista de relaciones
     */
    List<WalletRelation> findBySourceWalletAndRelationType(Wallet sourceWallet, WalletRelationType relationType);
    
    /**
     * Encuentra todas las relaciones de un tipo específico donde un monedero es el destino
     * @param targetWallet Monedero destino
     * @param relationType Tipo de relación
     * @return Lista de relaciones
     */
    List<WalletRelation> findByTargetWalletAndRelationType(Wallet targetWallet, WalletRelationType relationType);
    
    /**
     * Encuentra una relación específica entre dos monederos
     * @param sourceWallet Monedero origen
     * @param targetWallet Monedero destino
     * @return Relación si existe
     */
    Optional<WalletRelation> findBySourceWalletAndTargetWallet(Wallet sourceWallet, Wallet targetWallet);
    
    /**
     * Encuentra todas las relaciones con transferencia automática habilitada
     * @return Lista de relaciones
     */
    List<WalletRelation> findByAutoTransferEnabledTrue();
    
    /**
     * Encuentra todas las relaciones de un usuario
     * @param userId ID del usuario
     * @return Lista de relaciones
     */
    @Query("SELECT wr FROM WalletRelation wr WHERE wr.sourceWallet.user.id = :userId OR wr.targetWallet.user.id = :userId")
    List<WalletRelation> findAllByUserId(Long userId);
    
    /**
     * Verifica si existe una relación entre dos monederos
     * @param sourceWalletId ID del monedero origen
     * @param targetWalletId ID del monedero destino
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(wr) > 0 THEN true ELSE false END FROM WalletRelation wr WHERE wr.sourceWallet.id = :sourceWalletId AND wr.targetWallet.id = :targetWalletId")
    boolean existsRelationBetweenWallets(Long sourceWalletId, Long targetWalletId);
}
