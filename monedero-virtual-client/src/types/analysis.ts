import { TransactionType } from './transaction.types';

export interface TimeSeriesDataPoint {
  date: string;
  expenses: number;
  income: number;
}

export interface CategorySpending {
  categoryId: number;
  categoryName: string;
  iconName: string;
  colorCode: string;
  amount: number;
  percentage: number;
}

export interface SpendingTrend {
  description: string;
  amount: number;
  percentageChange: number;
  increase: boolean;
}

export interface SpendingAnalysisResponse {
  startDate: string;
  endDate: string;
  totalExpenses: number;
  totalIncome: number;
  netCashflow: number;
  categoryBreakdown: CategorySpending[];
  timeSeriesData: TimeSeriesDataPoint[];
  trends: SpendingTrend[];
}

export interface AnalysisTimeRange {
  startDate: string;
  endDate: string;
}

export enum AnalysisPeriod {
  LAST_WEEK = 'LAST_WEEK',
  LAST_MONTH = 'LAST_MONTH',
  LAST_THREE_MONTHS = 'LAST_THREE_MONTHS',
  LAST_SIX_MONTHS = 'LAST_SIX_MONTHS',
  LAST_YEAR = 'LAST_YEAR',
  CUSTOM = 'CUSTOM'
}

// --- NUEVO: Tipos para patrones de gasto ---
export interface PatternCategoryNode {
  categoryId: number;
  categoryName: string;
  iconName: string;
  colorCode: string;
  amount: number;
  percentage: number;
}

export interface PatternSpendingSequence {
  sourceCategoryId: number;
  sourceCategoryName: string;
  targetCategoryId: number;
  targetCategoryName: string;
  frequency: number;
}

export interface PatternCategoryCorrelation {
  category1Id: number;
  category1Name?: string;
  category2Id: number;
  category2Name?: string;
  correlationStrength: number;
}

export interface PatternCyclicalPattern {
  categoryId: number;
  averageAmount: number;
  frequency: string;
  regularity: number;
}

export interface TimeSeriesCategoryDataPoint {
  date: string;
  categoryId: number;
  categoryName: string;
  amount: number;
  transactionType: 'DEPOSIT' | 'WITHDRAWAL';
}

export interface SpendingPatternResponse {
  startDate: string;
  endDate: string;
  topCategories: PatternCategoryNode[];
  commonSequences: PatternSpendingSequence[];
  categoryCorrelations: PatternCategoryCorrelation[];
  cyclicalPatterns: PatternCyclicalPattern[];
  recommendations: string[];
  timeSeriesData: TimeSeriesCategoryDataPoint[];
}
