import { TransactionType } from './transaction.types';
import { Wallet } from './wallet.types';

/**
 * Tipos de recurrencia para transacciones programadas
 */
export enum RecurrenceType {
  ONCE = 'ONCE',
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY',
  YEARLY = 'YEARLY'
}

/**
 * Mapeo de tipos de recurrencia a descripciones en español
 */
export const RecurrenceTypeLabels: Record<RecurrenceType, string> = {
  [RecurrenceType.ONCE]: 'Una vez',
  [RecurrenceType.DAILY]: 'Diaria',
  [RecurrenceType.WEEKLY]: 'Semanal',
  [RecurrenceType.MONTHLY]: 'Mensual',
  [RecurrenceType.YEARLY]: 'Anual'
};

/**
 * Interfaz para transacciones programadas
 */
export interface ScheduledTransaction {
  id: number;
  type: TransactionType;
  amount: number;
  scheduledDate: string;
  creationDate: string;
  description?: string;
  wallet: Wallet;
  targetWalletId?: number;
  recurrenceType?: RecurrenceType;
  executed: boolean;
  executionDate?: string;
}

/**
 * Interfaz para solicitudes de creación de transacciones programadas
 */
export interface ScheduledTransactionRequest {
  type: TransactionType;
  amount: number;
  scheduledDate: string;
  description?: string;
  walletId: number;
  targetWalletId?: number;
  recurrenceType?: RecurrenceType;
}

/**
 * Interfaz para respuestas de transacciones programadas
 */
export interface ScheduledTransactionResponse {
  id: number;
  type: TransactionType;
  amount: number;
  scheduledDate: string;
  creationDate: string;
  description?: string;
  wallet: Wallet;
  targetWalletId?: number;
  recurrenceType?: RecurrenceType;
  executed: boolean;
  executionDate?: string;
}
