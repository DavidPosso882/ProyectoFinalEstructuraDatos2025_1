package org.example.service;

import org.example.dto.response.SpendingAnalysisResponse;
import org.example.dto.response.SpendingPatternResponse;

import java.time.LocalDateTime;

public interface SpendingAnalysisService {

    /**
     * Analiza los patrones de gasto de un usuario en un período de tiempo
     * @param userId ID del usuario
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Análisis de patrones de gasto
     */
    SpendingAnalysisResponse analyzeUserSpending(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Analiza los patrones de gasto de un monedero en un período de tiempo
     * @param walletId ID del monedero
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Análisis de patrones de gasto
     */
    SpendingAnalysisResponse analyzeWalletSpending(Long walletId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Analiza los patrones de gasto de un usuario en el último mes
     * @param userId ID del usuario
     * @return Análisis de patrones de gasto
     */
    SpendingAnalysisResponse analyzeUserSpendingLastMonth(Long userId);

    /**
     * Analiza los patrones de gasto de un usuario en el último año
     * @param userId ID del usuario
     * @return Análisis de patrones de gasto
     */
    SpendingAnalysisResponse analyzeUserSpendingLastYear(Long userId);

    /**
     * Compara los patrones de gasto de un usuario entre dos períodos de tiempo
     * @param userId ID del usuario
     * @param currentStartDate Fecha de inicio del período actual
     * @param currentEndDate Fecha de fin del período actual
     * @param previousStartDate Fecha de inicio del período anterior
     * @param previousEndDate Fecha de fin del período anterior
     * @return Análisis comparativo de patrones de gasto
     */
    SpendingAnalysisResponse compareUserSpending(Long userId,
                                               LocalDateTime currentStartDate, LocalDateTime currentEndDate,
                                               LocalDateTime previousStartDate, LocalDateTime previousEndDate);

    /**
     * Analiza patrones de gasto de un usuario en un período de tiempo
     * @param userId ID del usuario
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Análisis de patrones de gasto
     */
    SpendingPatternResponse analyzeUserSpendingPatterns(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Analiza patrones de gasto de un monedero en un período de tiempo
     * @param walletId ID del monedero
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Análisis de patrones de gasto
     */
    SpendingPatternResponse analyzeWalletSpendingPatterns(Long walletId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Analiza patrones de gasto de un usuario en el último mes
     * @param userId ID del usuario
     * @return Análisis de patrones de gasto
     */
    SpendingPatternResponse analyzeUserSpendingPatternsLastMonth(Long userId);

    /**
     * Analiza patrones de gasto de un usuario en el último año
     * @param userId ID del usuario
     * @return Análisis de patrones de gasto
     */
    SpendingPatternResponse analyzeUserSpendingPatternsLastYear(Long userId);
}
