import { Transaction, TransactionType } from '../types/transaction.types';

/**
 * Determina si una transacción es de entrada de dinero desde la perspectiva del usuario actual
 * @param transaction La transacción a evaluar
 * @param currentUserId ID del usuario actual
 * @returns true si es una entrada de dinero, false en caso contrario
 */
export const isIncomingTransaction = (transaction: Transaction, currentUserId: number): boolean => {
  switch (transaction.transactionType) {
    case TransactionType.DEPOSIT:
      // Los depósitos siempre son entrada de dinero
      return true;

    case TransactionType.TRANSFER:
      // Para transferencias, es entrada si el usuario es el destinatario
      return transaction.targetWallet?.user?.id === currentUserId;

    case TransactionType.POINTS_REDEMPTION:
      // Los canjes de puntos pueden ser entrada de dinero
      return transaction.targetWallet?.user?.id === currentUserId;

    case TransactionType.SYSTEM:
      // Las transacciones del sistema pueden ser entrada si el usuario es el destinatario
      return transaction.targetWallet?.user?.id === currentUserId;

    default:
      return false;
  }
};

/**
 * Determina si una transacción es de salida de dinero desde la perspectiva del usuario actual
 * @param transaction La transacción a evaluar
 * @param currentUserId ID del usuario actual
 * @returns true si es una salida de dinero, false en caso contrario
 */
export const isOutgoingTransaction = (transaction: Transaction, currentUserId: number): boolean => {
  switch (transaction.transactionType) {
    case TransactionType.WITHDRAWAL:
      // Los retiros siempre son salida de dinero
      return true;

    case TransactionType.TRANSFER:
      // Para transferencias, es salida si el usuario es el remitente
      return transaction.sourceWallet?.user?.id === currentUserId;

    case TransactionType.POINTS_REDEMPTION:
      // Los canjes de puntos pueden ser salida de dinero
      return transaction.sourceWallet?.user?.id === currentUserId;

    case TransactionType.SYSTEM:
      // Las transacciones del sistema pueden ser salida si el usuario es el remitente
      return transaction.sourceWallet?.user?.id === currentUserId;

    default:
      return false;
  }
};

/**
 * Determina si una transacción es una transferencia local (entre monederos del mismo usuario)
 * @param transaction La transacción a evaluar
 * @param currentUserId ID del usuario actual
 * @returns true si es una transferencia local, false en caso contrario
 */
export const isLocalTransfer = (transaction: Transaction, currentUserId: number): boolean => {
  if (transaction.transactionType !== TransactionType.TRANSFER) {
    return false;
  }

  // Es transferencia local si tanto el origen como el destino pertenecen al usuario actual
  return transaction.sourceWallet?.user?.id === currentUserId &&
         transaction.targetWallet?.user?.id === currentUserId;
};

/**
 * Determina si una transacción es una transferencia externa (entre diferentes usuarios)
 * @param transaction La transacción a evaluar
 * @param currentUserId ID del usuario actual
 * @returns true si es una transferencia externa, false en caso contrario
 */
export const isExternalTransfer = (transaction: Transaction, currentUserId: number): boolean => {
  if (transaction.transactionType !== TransactionType.TRANSFER) {
    return false;
  }

  // Es transferencia externa si uno de los monederos pertenece al usuario y el otro no
  const isUserSource = transaction.sourceWallet?.user?.id === currentUserId;
  const isUserTarget = transaction.targetWallet?.user?.id === currentUserId;

  return (isUserSource && !isUserTarget) || (!isUserSource && isUserTarget);
};

/**
 * Filtra transacciones para la pestaña "Depósitos"
 * Incluye: depósitos directos, transferencias recibidas, canjes de puntos recibidos, transacciones del sistema recibidas
 * @param transactions Lista de transacciones
 * @param currentUserId ID del usuario actual
 * @returns Transacciones filtradas
 */
export const filterDepositTransactions = (transactions: Transaction[], currentUserId: number): Transaction[] => {
  return transactions.filter(transaction => isIncomingTransaction(transaction, currentUserId));
};

/**
 * Filtra transacciones para la pestaña "Retiros"
 * Incluye: retiros directos, transferencias enviadas, canjes de puntos enviados, transacciones del sistema enviadas
 * @param transactions Lista de transacciones
 * @param currentUserId ID del usuario actual
 * @returns Transacciones filtradas
 */
export const filterWithdrawalTransactions = (transactions: Transaction[], currentUserId: number): Transaction[] => {
  return transactions.filter(transaction => isOutgoingTransaction(transaction, currentUserId));
};

/**
 * Filtra transacciones para la pestaña "Transferencias"
 * Incluye solo transferencias entre monederos locales (sin duplicar información)
 * @param transactions Lista de transacciones
 * @param currentUserId ID del usuario actual
 * @returns Transacciones filtradas
 */
export const filterTransferTransactions = (transactions: Transaction[], currentUserId: number): Transaction[] => {
  return transactions.filter(transaction => isLocalTransfer(transaction, currentUserId));
};

/**
 * Obtiene el tipo de transacción desde la perspectiva del usuario para mostrar en la UI
 * @param transaction La transacción a evaluar
 * @param currentUserId ID del usuario actual
 * @returns El tipo de transacción desde la perspectiva del usuario
 */
export const getTransactionTypeForUser = (transaction: Transaction, currentUserId: number): string => {
  if (isIncomingTransaction(transaction, currentUserId)) {
    return 'Entrada';
  } else if (isOutgoingTransaction(transaction, currentUserId)) {
    return 'Salida';
  } else if (isLocalTransfer(transaction, currentUserId)) {
    return 'Transferencia Local';
  } else {
    return transaction.transactionType;
  }
};
