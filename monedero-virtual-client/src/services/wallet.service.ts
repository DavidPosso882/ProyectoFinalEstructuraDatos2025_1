import ApiService from './api.service';
import { Wallet, CreateWalletRequest } from '../types/wallet.types';

/**
 * Servicio para gestionar monederos
 */
const WalletService = {
  /**
   * Obtiene todos los monederos del usuario
   * @returns Lista de monederos
   */
  getUserWallets: (): Promise<Wallet[]> => ApiService.get<Wallet[]>('/api/wallets'),

  /**
   * Obtiene un monedero por su ID
   * @param id ID del monedero
   * @returns Monedero
   */
  getWalletById: (id: number): Promise<Wallet> => ApiService.get<Wallet>(`/api/wallets/${id}`),

  /**
   * Crea un nuevo monedero
   * @param wallet Datos del monedero
   * @returns Monedero creado
   */
  createWallet: (wallet: CreateWalletRequest): Promise<Wallet> => ApiService.post<Wallet>('/api/wallets', wallet),

  /**
   * Actualiza un monedero existente
   * @param id ID del monedero
   * @param wallet Datos actualizados del monedero
   * @returns Monedero actualizado
   */
  updateWallet: (id: number, wallet: Partial<Wallet>): Promise<Wallet> => ApiService.put<Wallet>(`/api/wallets/${id}`, wallet),

  /**
   * Elimina un monedero
   * @param id ID del monedero
   */
  deleteWallet: (id: number): Promise<void> => ApiService.delete<void>(`/api/wallets/${id}`),

  /**
   * Transfiere fondos entre monederos (puede ser entre usuarios diferentes)
   * @param sourceWalletId ID del monedero origen
   * @param targetWalletId ID del monedero destino
   * @param amount Monto a transferir
   */
  transferBetweenWallets: (sourceWalletId: number, targetWalletId: number, amount: number): Promise<Wallet> =>
    ApiService.post<Wallet>(`/api/wallets/transfer?sourceWalletId=${sourceWalletId}&targetWalletId=${targetWalletId}&amount=${amount}`),

  /**
   * Transfiere fondos desde un monedero origen al monedero principal de un usuario destino
   * @param sourceWalletId ID del monedero origen
   * @param targetUserId ID del usuario destino
   * @param amount Monto a transferir
   */
  transferToUser: (sourceWalletId: number, targetUserId: number, amount: number): Promise<Wallet> =>
    ApiService.post<Wallet>(`/api/wallets/transfer-to-user?sourceWalletId=${sourceWalletId}&targetUserId=${targetUserId}&amount=${amount}`),
};

export default WalletService;
