import ApiService from './api.service';
import { 
  ScheduledTransactionRequest, 
  ScheduledTransactionResponse 
} from '../types/scheduledTransaction.types';

/**
 * Servicio para gestionar transacciones programadas
 */
class ScheduledTransactionService {
  /**
   * Crea una nueva transacción programada
   * @param request Datos de la transacción programada
   * @returns Transacción programada creada
   */
  static async createScheduledTransaction(
    request: ScheduledTransactionRequest
  ): Promise<ScheduledTransactionResponse> {
    return ApiService.post<ScheduledTransactionResponse>(
      '/api/scheduled-transactions',
      request
    );
  }

  /**
   * Obtiene todas las transacciones programadas del usuario
   * @returns Lista de transacciones programadas
   */
  static async getUserScheduledTransactions(): Promise<ScheduledTransactionResponse[]> {
    return ApiService.get<ScheduledTransactionResponse[]>(
      '/api/scheduled-transactions'
    );
  }

  /**
   * Obtiene una transacción programada por su ID
   * @param id ID de la transacción programada
   * @returns Transacción programada
   */
  static async getScheduledTransactionById(
    id: number
  ): Promise<ScheduledTransactionResponse> {
    return ApiService.get<ScheduledTransactionResponse>(
      `/api/scheduled-transactions/${id}`
    );
  }

  /**
   * Cancela una transacción programada
   * @param id ID de la transacción programada
   */
  static async cancelScheduledTransaction(id: number): Promise<void> {
    return ApiService.delete<void>(
      `/api/scheduled-transactions/${id}`
    );
  }

  /**
   * Obtiene las próximas transacciones programadas (para mostrar en el dashboard)
   * @param limit Número máximo de transacciones a obtener
   * @returns Lista de transacciones programadas
   */
  static async getUpcomingScheduledTransactions(
    limit: number = 5
  ): Promise<ScheduledTransactionResponse[]> {
    // Implementar cuando el backend tenga este endpoint
    // Por ahora, obtenemos todas y filtramos las primeras 'limit'
    const transactions = await this.getUserScheduledTransactions();
    return transactions
      .filter(t => !t.executed)
      .sort((a, b) => new Date(a.scheduledDate).getTime() - new Date(b.scheduledDate).getTime())
      .slice(0, limit);
  }
}

export default ScheduledTransactionService;
