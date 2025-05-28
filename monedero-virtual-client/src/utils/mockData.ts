import { SpendingAnalysisResponse } from '../types/analysis';
import { addDays, subDays, subMonths, format } from 'date-fns';

/**
 * Genera datos de ejemplo para el análisis de patrones de gasto
 * @param startDate Fecha de inicio
 * @param endDate Fecha de fin
 * @returns Datos de ejemplo para el análisis
 */
export const generateMockSpendingAnalysis = (
  startDate: Date = subMonths(new Date(), 1),
  endDate: Date = new Date()
): SpendingAnalysisResponse => {
  // Calcular número de días entre las fechas
  const daysDiff = Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
  
  // Generar datos de serie temporal
  const timeSeriesData = [];
  for (let i = 0; i <= daysDiff; i++) {
    const currentDate = addDays(startDate, i);
    
    // Generar valores aleatorios para gastos e ingresos
    const expenses = Math.random() * 100 + 50; // Entre 50 y 150
    const income = Math.random() * 200 + 100; // Entre 100 y 300
    
    timeSeriesData.push({
      date: format(currentDate, "yyyy-MM-dd'T'HH:mm:ss"),
      expenses,
      income
    });
  }
  
  // Calcular totales
  const totalExpenses = timeSeriesData.reduce((sum, point) => sum + point.expenses, 0);
  const totalIncome = timeSeriesData.reduce((sum, point) => sum + point.income, 0);
  const netCashflow = totalIncome - totalExpenses;
  
  // Generar desglose por categorías
  const categories = [
    { name: 'Alimentación', icon: 'restaurant', color: '#FF5722' },
    { name: 'Transporte', icon: 'directions_car', color: '#2196F3' },
    { name: 'Entretenimiento', icon: 'movie', color: '#9C27B0' },
    { name: 'Servicios', icon: 'power', color: '#FFC107' },
    { name: 'Salud', icon: 'local_hospital', color: '#4CAF50' }
  ];
  
  const categoryBreakdown = categories.map((category, index) => {
    const amount = (totalExpenses / categories.length) * (1 + (Math.random() * 0.4 - 0.2)); // Variación del 20%
    return {
      categoryId: index + 1,
      categoryName: category.name,
      iconName: category.icon,
      colorCode: category.color,
      amount,
      percentage: (amount / totalExpenses) * 100
    };
  });
  
  // Generar tendencias
  const previousTotalExpenses = totalExpenses * (1 + (Math.random() * 0.3 - 0.15)); // Variación del 15%
  const previousTotalIncome = totalIncome * (1 + (Math.random() * 0.3 - 0.15)); // Variación del 15%
  const previousNetCashflow = previousTotalIncome - previousTotalExpenses;
  
  const expenseTrend = {
    description: 'Gastos totales',
    amount: totalExpenses,
    percentageChange: ((totalExpenses - previousTotalExpenses) / previousTotalExpenses) * 100,
    increase: totalExpenses > previousTotalExpenses
  };
  
  const incomeTrend = {
    description: 'Ingresos totales',
    amount: totalIncome,
    percentageChange: ((totalIncome - previousTotalIncome) / previousTotalIncome) * 100,
    increase: totalIncome > previousTotalIncome
  };
  
  const cashflowTrend = {
    description: 'Flujo de caja neto',
    amount: netCashflow,
    percentageChange: ((netCashflow - previousNetCashflow) / Math.abs(previousNetCashflow)) * 100,
    increase: netCashflow > previousNetCashflow
  };
  
  return {
    startDate: format(startDate, "yyyy-MM-dd'T'HH:mm:ss"),
    endDate: format(endDate, "yyyy-MM-dd'T'HH:mm:ss"),
    totalExpenses,
    totalIncome,
    netCashflow,
    categoryBreakdown,
    timeSeriesData,
    trends: [expenseTrend, incomeTrend, cashflowTrend]
  };
};
