package org.example.service;

import org.example.dto.request.WalletRequest;
import org.example.dto.response.WalletResponse;
import org.example.model.Currency;
import org.example.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    /**
     * Obtiene todos los monederos de un usuario
     * @param userId ID del usuario
     * @return Lista de monederos
     */
    List<WalletResponse> getUserWallets(Long userId);

    /**
     * Obtiene un monedero por su ID
     * @param walletId ID del monedero
     * @param userId ID del usuario propietario
     * @return Monedero encontrado
     */
    WalletResponse getWalletById(Long walletId, Long userId);

    /**
     * Crea un nuevo monedero para un usuario
     * @param walletRequest Datos del monedero
     * @param userId ID del usuario propietario
     * @return Monedero creado
     */
    WalletResponse createWallet(WalletRequest walletRequest, Long userId);

    /**
     * Crea un nuevo monedero para un usuario con una moneda específica
     * @param walletRequest Datos del monedero
     * @param userId ID del usuario propietario
     * @param currencyCode Código de la moneda
     * @return Monedero creado
     */
    WalletResponse createWallet(WalletRequest walletRequest, Long userId, String currencyCode);

    /**
     * Elimina un monedero
     * @param walletId ID del monedero
     * @param userId ID del usuario propietario
     */
    void deleteWallet(Long walletId, Long userId);

    /**
     * Actualiza el saldo de un monedero
     * @param walletId ID del monedero
     * @param amount Cantidad a añadir (positiva) o restar (negativa)
     * @return Monedero actualizado
     */
    Wallet updateWalletBalance(Long walletId, BigDecimal amount);

    /**
     * Transfiere fondos entre monederos de diferentes usuarios
     * @param sourceWalletId ID del monedero origen
     * @param targetWalletId ID del monedero destino
     * @param amount Cantidad a transferir en la moneda del monedero origen
     * @param userId ID del usuario autenticado (debe ser dueño del monedero origen)
     * @return Monedero origen actualizado
     */
    Wallet transferBetweenWallets(Long sourceWalletId, Long targetWalletId, BigDecimal amount, Long userId);

    /**
     * Verifica si un monedero tiene saldo suficiente
     * @param walletId ID del monedero
     * @param amount Cantidad a verificar
     * @return true si hay saldo suficiente, false en caso contrario
     */
    boolean hasWalletSufficientBalance(Long walletId, BigDecimal amount);

    /**
     * Guarda un monedero
     * @param wallet Monedero a guardar
     * @return Monedero guardado
     */
    Wallet saveWallet(Wallet wallet);

    /**
     * Obtiene todos los monederos de un usuario con una moneda específica
     * @param userId ID del usuario
     * @param currencyCode Código de la moneda
     * @return Lista de monederos
     */
    List<WalletResponse> getUserWalletsByCurrency(Long userId, String currencyCode);

    /**
     * Convierte un objeto Wallet a WalletResponse
     * @param wallet Monedero a convertir
     * @return WalletResponse
     */
    WalletResponse convertToWalletResponse(Wallet wallet);

    /**
     * Actualiza un monedero existente
     * @param walletId ID del monedero
     * @param walletRequest Datos actualizados
     * @param userId ID del usuario propietario
     * @return Monedero actualizado
     */
    WalletResponse updateWallet(Long walletId, WalletRequest walletRequest, Long userId);

    /**
     * Transfiere fondos desde un monedero origen al monedero principal (PRIMARY) de un usuario destino
     * @param sourceWalletId ID del monedero origen
     * @param targetUserId ID del usuario destino
     * @param amount Cantidad a transferir
     * @param userId ID del usuario autenticado (debe ser dueño del monedero origen)
     * @return Monedero origen actualizado
     */
    Wallet transferToUser(Long sourceWalletId, Long targetUserId, BigDecimal amount, Long userId);
}
