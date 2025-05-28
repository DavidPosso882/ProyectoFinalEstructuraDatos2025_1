import ApiService from './api.service';
import { SpendingAnalysisResponse, AnalysisTimeRange, SpendingPatternResponse } from '../types/analysis';
import { generateMockSpendingAnalysis } from '../utils/mockData';

// Bandera para usar datos de ejemplo en desarrollo
const USE_MOCK_DATA = false; // Cambiar a false cuando el backend esté listo

class AnalysisService {
  /**
   * Obtiene el análisis de patrones de gasto del usuario para un período específico
   * @param startDate Fecha de inicio en formato ISO
   * @param endDate Fecha de fin en formato ISO
   * @returns Respuesta con el análisis de patrones de gasto
   */
  static async getUserSpendingAnalysis(
    startDate: string,
    endDate: string
  ): Promise<SpendingAnalysisResponse> {
    if (USE_MOCK_DATA) {
      // Usar datos de ejemplo en desarrollo
      return Promise.resolve(generateMockSpendingAnalysis(
        new Date(startDate),
        new Date(endDate)
      ));
    }
    
    return ApiService.get<SpendingAnalysisResponse>(
      `/api/analysis/user?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`
    );
  }

  /**
   * Obtiene el análisis de patrones de gasto del último mes
   * @returns Respuesta con el análisis de patrones de gasto
   */
  static async getUserSpendingLastMonth(): Promise<SpendingAnalysisResponse> {
    if (USE_MOCK_DATA) {
      // Usar datos de ejemplo en desarrollo
      const endDate = new Date();
      const startDate = new Date();
      startDate.setMonth(startDate.getMonth() - 1);
      
      return Promise.resolve(generateMockSpendingAnalysis(startDate, endDate));
    }
    
    return ApiService.get<SpendingAnalysisResponse>('/api/analysis/user/month');
  }

  /**
   * Obtiene el análisis de patrones de gasto del último año
   * @returns Respuesta con el análisis de patrones de gasto
   */
  static async getUserSpendingLastYear(): Promise<SpendingAnalysisResponse> {
    if (USE_MOCK_DATA) {
      // Usar datos de ejemplo en desarrollo
      const endDate = new Date();
      const startDate = new Date();
      startDate.setFullYear(startDate.getFullYear() - 1);
      
      return Promise.resolve(generateMockSpendingAnalysis(startDate, endDate));
    }
    
    return ApiService.get<SpendingAnalysisResponse>('/api/analysis/user/year');
  }

  /**
   * Obtiene el análisis de patrones de gasto para un monedero específico
   * @param walletId ID del monedero
   * @param startDate Fecha de inicio en formato ISO
   * @param endDate Fecha de fin en formato ISO
   * @returns Respuesta con el análisis de patrones de gasto
   */
  static async getWalletSpendingAnalysis(
    walletId: number,
    startDate: string,
    endDate: string
  ): Promise<SpendingAnalysisResponse> {
    if (USE_MOCK_DATA) {
      // Usar datos de ejemplo en desarrollo
      return Promise.resolve(generateMockSpendingAnalysis(
        new Date(startDate),
        new Date(endDate)
      ));
    }
    
    return ApiService.get<SpendingAnalysisResponse>(
      `/api/analysis/wallet/${walletId}?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`
    );
  }

  /**
   * Compara los patrones de gasto entre dos períodos
   * @param currentPeriod Período actual
   * @param previousPeriod Período anterior
   * @returns Respuesta con el análisis comparativo
   */
  static async compareUserSpending(
    currentPeriod: AnalysisTimeRange,
    previousPeriod: AnalysisTimeRange
  ): Promise<SpendingAnalysisResponse> {
    if (USE_MOCK_DATA) {
      // Usar datos de ejemplo en desarrollo
      const response = generateMockSpendingAnalysis(
        new Date(currentPeriod.startDate),
        new Date(currentPeriod.endDate)
      );
      
      // Generar tendencias más realistas para la comparación
      const previousAnalysis = generateMockSpendingAnalysis(
        new Date(previousPeriod.startDate),
        new Date(previousPeriod.endDate)
      );
      
      // Calcular tendencias basadas en la comparación real
      response.trends = [
        {
          description: 'Gastos totales',
          amount: response.totalExpenses,
          percentageChange: ((response.totalExpenses - previousAnalysis.totalExpenses) / previousAnalysis.totalExpenses) * 100,
          increase: response.totalExpenses > previousAnalysis.totalExpenses
        },
        {
          description: 'Ingresos totales',
          amount: response.totalIncome,
          percentageChange: ((response.totalIncome - previousAnalysis.totalIncome) / previousAnalysis.totalIncome) * 100,
          increase: response.totalIncome > previousAnalysis.totalIncome
        },
        {
          description: 'Flujo de caja neto',
          amount: response.netCashflow,
          percentageChange: ((response.netCashflow - previousAnalysis.netCashflow) / Math.abs(previousAnalysis.netCashflow)) * 100,
          increase: response.netCashflow > previousAnalysis.netCashflow
        }
      ];
      
      return Promise.resolve(response);
    }
    
    return ApiService.get<SpendingAnalysisResponse>(
      `/api/analysis/user/compare?currentStartDate=${encodeURIComponent(currentPeriod.startDate)}&currentEndDate=${encodeURIComponent(currentPeriod.endDate)}&previousStartDate=${encodeURIComponent(previousPeriod.startDate)}&previousEndDate=${encodeURIComponent(previousPeriod.endDate)}`
    );
  }

  /**
   * Obtiene fechas para un período de análisis predefinido
   * @param days Número de días hacia atrás
   * @returns Objeto con fechas de inicio y fin
   */
  static getDateRangeFromDays(days: number): AnalysisTimeRange {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - days);
    
    return {
      startDate: startDate.toISOString(),
      endDate: endDate.toISOString()
    };
  }

  /**
   * Obtiene fechas para un período de análisis predefinido en meses
   * @param months Número de meses hacia atrás
   * @returns Objeto con fechas de inicio y fin
   */
  static getDateRangeFromMonths(months: number): AnalysisTimeRange {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setMonth(startDate.getMonth() - months);
    
    return {
      startDate: startDate.toISOString(),
      endDate: endDate.toISOString()
    };
  }

  /**
   * Obtiene el patrón de gasto del usuario para un período específico (estructura de datos propia)
   * @param startDate Fecha de inicio en formato ISO
   * @param endDate Fecha de fin en formato ISO
   * @returns Respuesta con el patrón de gasto
   */
  static async getUserSpendingPattern(
    startDate: string,
    endDate: string
  ): Promise<SpendingPatternResponse> {
    return ApiService.get<SpendingPatternResponse>(
      `/api/analysis/user/pattern?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`
    );
  }
}

export default AnalysisService;
