export enum TransactionType {
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  TRANSFER = 'TRANSFER',
  PAYMENT = 'PAYMENT',
  POINTS_REDEMPTION = 'POINTS_REDEMPTION',
  SCHEDULED_TRANSFER = 'SCHEDULED_TRANSFER',
  SYSTEM = 'SYSTEM'
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

export interface Transaction {
  id: number;
  amount: number;
  description: string;
  transactionType: TransactionType;
  status: TransactionStatus;
  sourceWalletId?: number;
  destinationWalletId?: number;
  createdAt: string;
  updatedAt: string;
  scheduledDate?: string;
  categoryId?: number;
  metadata?: Record<string, any>;
  pointsEarned?: number;
  reversed?: boolean;
  referenceId?: string;
  sourceWallet?: {
    id: number;
    name: string;
    user: {
      id: number;
      username: string;
    };
  };
  targetWallet?: {
    id: number;
    name: string;
    user: {
      id: number;
      username: string;
    };
  };
}

export interface TransactionRequest {
  amount: number;
  description: string;
  transactionType: TransactionType;
  sourceWalletId?: number;
  destinationWalletId?: number;
  scheduledDate?: string;
  categoryId?: number;
  metadata?: Record<string, any>;
}

export interface TransactionResponse {
  id: number;
  amount: number;
  description: string;
  transactionType: TransactionType;
  status: TransactionStatus;
  sourceWalletId?: number;
  destinationWalletId?: number;
  createdAt: string;
  updatedAt: string;
  scheduledDate?: string;
  categoryId?: number;
  metadata?: Record<string, any>;
}
