export type WalletType = 'PRIMARY' | 'SAVINGS' | 'EXPENSES' | 'INVESTMENT' | 'CUSTOM';

export interface Wallet {
  id: number;
  name: string;
  balance: number;
  type: WalletType;
  description: string;
  isDefault?: boolean;
  isFavorite?: boolean;
  createdAt: string;
  updatedAt: string;
  monthlyInflow?: number;
  monthlyOutflow?: number;
}

export interface CreateWalletRequest {
  name: string;
  walletType: WalletType;
  description?: string;
  initialBalance?: number;
}
