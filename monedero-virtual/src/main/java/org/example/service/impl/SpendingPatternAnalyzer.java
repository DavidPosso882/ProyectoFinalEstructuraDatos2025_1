package org.example.service.impl;

import org.example.datastructure.graph.DirectedGraph;
import org.example.datastructure.graph.EdgeTriple;
import org.example.dto.response.SpendingPatternResponse;
import org.example.model.Transaction;
import org.example.model.TransactionCategory;
import org.example.model.TransactionCategoryMapping;
import org.example.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Componente para analizar patrones de gasto utilizando grafos
 */
@Component
public class SpendingPatternAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(SpendingPatternAnalyzer.class);
    
    /**
     * Analiza patrones de gasto entre categorías utilizando un grafo dirigido
     * @param transactions Lista de transacciones
     * @param categoryMappings Lista de mapeos de categorías
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @param userId ID del usuario
     * @return Respuesta con los patrones de gasto
     */
    public SpendingPatternResponse analyzeSpendingPatterns(
            List<Transaction> transactions,
            List<TransactionCategoryMapping> categoryMappings,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long userId) {
        
        logger.info("Analizando patrones de gasto entre {} transacciones y {} categorías", 
            transactions.size(), categoryMappings.size());
        
        // Construir el grafo de categorías
        DirectedGraph<Long> categoryGraph = buildCategoryGraph(categoryMappings);
        
        // Analizar patrones
        SpendingPatternResponse response = new SpendingPatternResponse();
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        
        // Identificar categorías principales
        List<SpendingPatternResponse.CategoryNode> topCategories = identifyTopCategories(categoryMappings);
        response.setTopCategories(topCategories);
        
        // Identificar secuencias de gasto comunes
        List<SpendingPatternResponse.SpendingSequence> commonSequences = identifyCommonSequences(categoryGraph, categoryMappings);
        response.setCommonSequences(commonSequences);
        
        // Identificar correlaciones entre categorías
        List<SpendingPatternResponse.CategoryCorrelation> correlations = identifyCategoryCorrelations(categoryGraph);
        response.setCategoryCorrelations(correlations);
        
        // Identificar patrones cíclicos
        List<SpendingPatternResponse.CyclicalPattern> cyclicalPatterns = identifyCyclicalPatterns(transactions, categoryMappings);
        response.setCyclicalPatterns(cyclicalPatterns);
        
        // Generar recomendaciones basadas en patrones
        List<String> recommendations = generateRecommendations(topCategories, commonSequences, correlations, cyclicalPatterns);
        response.setRecommendations(recommendations);
        
        // --- NUEVO: Generar serie temporal de patrones de gasto por categoría principal ---
        response.setTimeSeriesData(generateTimeSeriesDataByCategory(transactions, topCategories, startDate, endDate, userId));
        
        return response;
    }
    
    /**
     * Construye un grafo dirigido de categorías basado en la secuencia temporal de transacciones
     * @param categoryMappings Lista de mapeos de categorías
     * @return Grafo dirigido de categorías
     */
    private DirectedGraph<Long> buildCategoryGraph(List<TransactionCategoryMapping> categoryMappings) {
        DirectedGraph<Long> graph = new DirectedGraph<>();
        
        // Ordenar mapeos por fecha de transacción
        List<TransactionCategoryMapping> sortedMappings = categoryMappings.stream()
                .sorted(Comparator.comparing(m -> m.getTransaction().getTransactionDate()))
                .collect(Collectors.toList());
        
        // Añadir vértices al grafo (IDs de categorías)
        for (TransactionCategoryMapping mapping : sortedMappings) {
            graph.addVertex(mapping.getCategory().getId());
        }
        
        // Añadir aristas basadas en la secuencia temporal
        for (int i = 0; i < sortedMappings.size() - 1; i++) {
            TransactionCategoryMapping current = sortedMappings.get(i);
            TransactionCategoryMapping next = sortedMappings.get(i + 1);
            
            // Solo conectar si son del mismo usuario y tipo de transacción (gasto)
            if (current.getTransaction().getType() == TransactionType.WITHDRAWAL &&
                next.getTransaction().getType() == TransactionType.WITHDRAWAL) {
                
                Long sourceId = current.getCategory().getId();
                Long targetId = next.getCategory().getId();
                
                // Añadir arista o incrementar peso si ya existe
                if (graph.hasEdge(sourceId, targetId)) {
                    double currentWeight = graph.getEdgeWeight(sourceId, targetId);
                    graph.addEdge(sourceId, targetId, currentWeight + 1.0);
                } else {
                    graph.addEdge(sourceId, targetId, 1.0);
                }
            }
        }
        
        return graph;
    }
    
    /**
     * Identifica las categorías principales de gasto
     * @param categoryMappings Lista de mapeos de categorías
     * @return Lista de nodos de categoría
     */
    private List<SpendingPatternResponse.CategoryNode> identifyTopCategories(List<TransactionCategoryMapping> categoryMappings) {
        // Agrupar por categoría y sumar montos
        Map<Long, BigDecimal> categoryAmounts = new HashMap<>();
        Map<Long, TransactionCategory> categoryMap = new HashMap<>();
        
        for (TransactionCategoryMapping mapping : categoryMappings) {
            if (mapping.getTransaction().getType() == TransactionType.WITHDRAWAL) {
                Long categoryId = mapping.getCategory().getId();
                BigDecimal amount = mapping.getTransaction().getAmount();
                
                categoryAmounts.merge(categoryId, amount, BigDecimal::add);
                categoryMap.putIfAbsent(categoryId, mapping.getCategory());
            }
        }
        
        // Calcular el total de gastos
        BigDecimal totalExpenses = categoryAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Crear nodos de categoría
        List<SpendingPatternResponse.CategoryNode> categoryNodes = new ArrayList<>();
        
        for (Map.Entry<Long, BigDecimal> entry : categoryAmounts.entrySet()) {
            Long categoryId = entry.getKey();
            BigDecimal amount = entry.getValue();
            TransactionCategory category = categoryMap.get(categoryId);
            
            SpendingPatternResponse.CategoryNode node = new SpendingPatternResponse.CategoryNode();
            node.setCategoryId(categoryId);
            node.setCategoryName(category.getName());
            node.setIconName(category.getIconName());
            node.setColorCode(category.getColorCode());
            node.setAmount(amount);
            
            // Calcular porcentaje
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                double percentage = amount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                node.setPercentage(percentage);
            }
            
            categoryNodes.add(node);
        }
        
        // Ordenar por monto (mayor a menor) y limitar a las 5 principales
        categoryNodes.sort((c1, c2) -> c2.getAmount().compareTo(c1.getAmount()));
        
        return categoryNodes.stream()
                .limit(5)
                .collect(Collectors.toList());
    }
    
    /**
     * Identifica secuencias comunes de gasto
     * @param categoryGraph Grafo de categorías
     * @param categoryMappings Lista de mapeos de categorías
     * @return Lista de secuencias de gasto
     */
    private List<SpendingPatternResponse.SpendingSequence> identifyCommonSequences(
            DirectedGraph<Long> categoryGraph, List<TransactionCategoryMapping> categoryMappings) {
        
        List<SpendingPatternResponse.SpendingSequence> sequences = new ArrayList<>();
        
        // Obtener todas las aristas del grafo
        List<EdgeTriple<Long>> allEdges = categoryGraph.getAllEdges();
        
        // Ordenar por peso (frecuencia) descendente
        allEdges.sort((e1, e2) -> Double.compare(e2.getWeight(), e1.getWeight()));
        
        // Crear un mapa de categorías para acceso rápido
        Map<Long, TransactionCategory> categoryMap = categoryMappings.stream()
                .collect(Collectors.toMap(
                        mapping -> mapping.getCategory().getId(),
                        mapping -> mapping.getCategory(),
                        (c1, c2) -> c1
                ));
        
        // Convertir las aristas más frecuentes en secuencias
        for (int i = 0; i < Math.min(5, allEdges.size()); i++) {
            EdgeTriple<Long> edge = allEdges.get(i);
            
            TransactionCategory sourceCategory = categoryMap.get(edge.getSource());
            TransactionCategory targetCategory = categoryMap.get(edge.getDestination());
            
            if (sourceCategory != null && targetCategory != null) {
                SpendingPatternResponse.SpendingSequence sequence = new SpendingPatternResponse.SpendingSequence();
                sequence.setSourceCategoryId(sourceCategory.getId());
                sequence.setSourceCategoryName(sourceCategory.getName());
                sequence.setTargetCategoryId(targetCategory.getId());
                sequence.setTargetCategoryName(targetCategory.getName());
                sequence.setFrequency((int) edge.getWeight());
                
                sequences.add(sequence);
            }
        }
        
        return sequences;
    }
    
    /**
     * Identifica correlaciones entre categorías de gasto
     * @param categoryGraph Grafo de categorías
     * @return Lista de correlaciones entre categorías
     */
    private List<SpendingPatternResponse.CategoryCorrelation> identifyCategoryCorrelations(DirectedGraph<Long> categoryGraph) {
        List<SpendingPatternResponse.CategoryCorrelation> correlations = new ArrayList<>();
        
        // Obtener todos los vértices del grafo
        Set<Long> vertices = categoryGraph.getVertices();
        
        // Para cada par de vértices, calcular la correlación
        for (Long source : vertices) {
            for (Long target : vertices) {
                if (!source.equals(target)) {
                    // Verificar si hay aristas en ambas direcciones
                    boolean sourceToTarget = categoryGraph.hasEdge(source, target);
                    boolean targetToSource = categoryGraph.hasEdge(target, source);
                    
                    if (sourceToTarget && targetToSource) {
                        double sourceToTargetWeight = categoryGraph.getEdgeWeight(source, target);
                        double targetToSourceWeight = categoryGraph.getEdgeWeight(target, source);
                        
                        // Calcular la fuerza de la correlación
                        double correlationStrength = (sourceToTargetWeight + targetToSourceWeight) / 2.0;
                        
                        // Solo incluir correlaciones fuertes
                        if (correlationStrength >= 2.0) {
                            SpendingPatternResponse.CategoryCorrelation correlation = new SpendingPatternResponse.CategoryCorrelation();
                            correlation.setCategory1Id(source);
                            correlation.setCategory2Id(target);
                            correlation.setCorrelationStrength(correlationStrength);
                            
                            correlations.add(correlation);
                        }
                    }
                }
            }
        }
        
        // Ordenar por fuerza de correlación descendente y limitar a las 5 principales
        correlations.sort((c1, c2) -> Double.compare(c2.getCorrelationStrength(), c1.getCorrelationStrength()));
        
        return correlations.stream()
                .limit(5)
                .collect(Collectors.toList());
    }
    
    /**
     * Identifica patrones cíclicos de gasto
     * @param transactions Lista de transacciones
     * @param categoryMappings Lista de mapeos de categorías
     * @return Lista de patrones cíclicos
     */
    private List<SpendingPatternResponse.CyclicalPattern> identifyCyclicalPatterns(
            List<Transaction> transactions, List<TransactionCategoryMapping> categoryMappings) {
        
        List<SpendingPatternResponse.CyclicalPattern> cyclicalPatterns = new ArrayList<>();
        
        // Implementación simplificada: detectar categorías con gastos regulares
        // Agrupar transacciones por categoría y mes
        Map<Long, Map<Integer, List<Transaction>>> categoryMonthlyTransactions = new HashMap<>();
        
        for (TransactionCategoryMapping mapping : categoryMappings) {
            Transaction transaction = mapping.getTransaction();
            
            if (transaction.getType() == TransactionType.WITHDRAWAL) {
                Long categoryId = mapping.getCategory().getId();
                int month = transaction.getTransactionDate().getMonthValue();
                
                categoryMonthlyTransactions
                    .computeIfAbsent(categoryId, k -> new HashMap<>())
                    .computeIfAbsent(month, k -> new ArrayList<>())
                    .add(transaction);
            }
        }
        
        // Identificar categorías con transacciones en al menos 3 meses consecutivos
        for (Map.Entry<Long, Map<Integer, List<Transaction>>> entry : categoryMonthlyTransactions.entrySet()) {
            Long categoryId = entry.getKey();
            Map<Integer, List<Transaction>> monthlyTransactions = entry.getValue();
            
            if (monthlyTransactions.size() >= 3) {
                // Calcular la regularidad (desviación estándar de los montos)
                List<BigDecimal> monthlyAmounts = new ArrayList<>();
                
                for (List<Transaction> monthTransactions : monthlyTransactions.values()) {
                    BigDecimal monthTotal = monthTransactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    monthlyAmounts.add(monthTotal);
                }
                
                // Calcular la media
                BigDecimal sum = monthlyAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal mean = sum.divide(BigDecimal.valueOf(monthlyAmounts.size()), 2, RoundingMode.HALF_UP);
                
                // Calcular la desviación estándar
                double variance = monthlyAmounts.stream()
                        .mapToDouble(amount -> Math.pow(amount.subtract(mean).doubleValue(), 2))
                        .average()
                        .orElse(0.0);
                
                double stdDev = Math.sqrt(variance);
                
                // Calcular el coeficiente de variación (CV)
                double cv = stdDev / mean.doubleValue();
                
                // Si el CV es bajo, consideramos que hay un patrón cíclico
                if (cv < 0.3) {
                    SpendingPatternResponse.CyclicalPattern pattern = new SpendingPatternResponse.CyclicalPattern();
                    pattern.setCategoryId(categoryId);
                    pattern.setAverageAmount(mean);
                    pattern.setFrequency("Mensual");
                    pattern.setRegularity(1.0 - cv); // Convertir CV a una medida de regularidad
                    
                    cyclicalPatterns.add(pattern);
                }
            }
        }
        
        // Ordenar por regularidad descendente
        cyclicalPatterns.sort((p1, p2) -> Double.compare(p2.getRegularity(), p1.getRegularity()));
        
        return cyclicalPatterns;
    }
    
    /**
     * Genera recomendaciones basadas en los patrones identificados
     */
    private List<String> generateRecommendations(
            List<SpendingPatternResponse.CategoryNode> topCategories,
            List<SpendingPatternResponse.SpendingSequence> commonSequences,
            List<SpendingPatternResponse.CategoryCorrelation> correlations,
            List<SpendingPatternResponse.CyclicalPattern> cyclicalPatterns) {
        
        List<String> recommendations = new ArrayList<>();
        
        // Recomendaciones basadas en categorías principales
        if (!topCategories.isEmpty()) {
            SpendingPatternResponse.CategoryNode topCategory = topCategories.get(0);
            if (topCategory.getPercentage() > 30) {
                recommendations.add("Considera reducir tus gastos en " + topCategory.getCategoryName() + 
                    ", que representa el " + String.format("%.1f", topCategory.getPercentage()) + "% de tus gastos totales.");
            }
        }
        
        // Recomendaciones basadas en secuencias comunes
        if (!commonSequences.isEmpty()) {
            SpendingPatternResponse.SpendingSequence topSequence = commonSequences.get(0);
            recommendations.add("Hemos notado que frecuentemente gastas en " + topSequence.getTargetCategoryName() + 
                " después de " + topSequence.getSourceCategoryName() + ". Considera planificar estos gastos juntos.");
        }
        
        // Recomendaciones basadas en correlaciones
        if (!correlations.isEmpty()) {
            recommendations.add("Hay una fuerte correlación entre algunas categorías de gasto. " +
                "Considera revisar si puedes optimizar estos gastos relacionados.");
        }
        
        // Recomendaciones basadas en patrones cíclicos
        if (!cyclicalPatterns.isEmpty()) {
            SpendingPatternResponse.CyclicalPattern topPattern = cyclicalPatterns.get(0);
            recommendations.add("Tienes un gasto regular mensual de aproximadamente " + 
                topPattern.getAverageAmount() + ". Considera crear un presupuesto específico para esta categoría.");
        }
        
        return recommendations;
    }
    
    /**
     * Genera una serie temporal de ingresos y egresos por fecha
     */
    private List<SpendingPatternResponse.TimeSeriesCategoryDataPoint> generateTimeSeriesDataByCategory(
            List<Transaction> transactions,
            List<SpendingPatternResponse.CategoryNode> topCategories,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long userId) {
        List<SpendingPatternResponse.TimeSeriesCategoryDataPoint> result = new ArrayList<>();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
        Map<String, Map<LocalDateTime, java.math.BigDecimal>> ingresosPorFecha = new HashMap<>();
        Map<String, Map<LocalDateTime, java.math.BigDecimal>> egresosPorFecha = new HashMap<>();
        ingresosPorFecha.put("Ingresos", new HashMap<>());
        egresosPorFecha.put("Egresos", new HashMap<>());
        for (int i = 0; i <= daysBetween; i++) {
            LocalDateTime date = startDate.plusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0);
            ingresosPorFecha.get("Ingresos").put(date, java.math.BigDecimal.ZERO);
            egresosPorFecha.get("Egresos").put(date, java.math.BigDecimal.ZERO);
        }
        logger.info("Fechas procesadas para análisis de patrones:");
        for (LocalDateTime d : ingresosPorFecha.get("Ingresos").keySet()) {
            logger.info("Fecha: {}", d);
        }
        for (Transaction t : transactions) {
            LocalDateTime date = t.getTransactionDate().withHour(0).withMinute(0).withSecond(0).withNano(0);
            logger.info("Transacción: id={}, fecha={}, tipo={}, monto={}, sourceWalletUser={}, targetWalletUser={}",
                t.getId(), t.getTransactionDate(), t.getType(), t.getAmount(),
                t.getSourceWallet() != null && t.getSourceWallet().getUser() != null ? t.getSourceWallet().getUser().getId() : null,
                t.getTargetWallet() != null && t.getTargetWallet().getUser() != null ? t.getTargetWallet().getUser().getId() : null
            );
            if (t.getType() == TransactionType.DEPOSIT && t.getTargetWallet() != null && t.getTargetWallet().getUser() != null && t.getTargetWallet().getUser().getId().equals(userId)) {
                ingresosPorFecha.get("Ingresos").merge(date, t.getAmount(), java.math.BigDecimal::add);
            } else if (t.getType() == TransactionType.WITHDRAWAL && t.getSourceWallet() != null && t.getSourceWallet().getUser() != null && t.getSourceWallet().getUser().getId().equals(userId)) {
                egresosPorFecha.get("Egresos").merge(date, t.getAmount(), java.math.BigDecimal::add);
            } else if (t.getType() == TransactionType.TRANSFER) {
                if (t.getSourceWallet() != null && t.getSourceWallet().getUser() != null && t.getSourceWallet().getUser().getId().equals(userId)) {
                    egresosPorFecha.get("Egresos").merge(date, t.getAmount(), java.math.BigDecimal::add);
                } else if (t.getTargetWallet() != null && t.getTargetWallet().getUser() != null && t.getTargetWallet().getUser().getId().equals(userId)) {
                    ingresosPorFecha.get("Ingresos").merge(date, t.getAmount(), java.math.BigDecimal::add);
                }
            }
        }
        logger.info("Montos sumados por fecha (Ingresos):");
        for (Map.Entry<LocalDateTime, java.math.BigDecimal> entry : ingresosPorFecha.get("Ingresos").entrySet()) {
            logger.info("Fecha: {}, Ingresos: {}", entry.getKey(), entry.getValue());
        }
        logger.info("Montos sumados por fecha (Egresos):");
        for (Map.Entry<LocalDateTime, java.math.BigDecimal> entry : egresosPorFecha.get("Egresos").entrySet()) {
            logger.info("Fecha: {}, Egresos: {}", entry.getKey(), entry.getValue());
        }
        for (Map.Entry<LocalDateTime, java.math.BigDecimal> entry : ingresosPorFecha.get("Ingresos").entrySet()) {
            SpendingPatternResponse.TimeSeriesCategoryDataPoint point = new SpendingPatternResponse.TimeSeriesCategoryDataPoint();
            point.setDate(entry.getKey());
            point.setCategoryName("Ingresos");
            point.setAmount(entry.getValue());
            point.setTransactionType("DEPOSIT");
            result.add(point);
        }
        for (Map.Entry<LocalDateTime, java.math.BigDecimal> entry : egresosPorFecha.get("Egresos").entrySet()) {
            SpendingPatternResponse.TimeSeriesCategoryDataPoint point = new SpendingPatternResponse.TimeSeriesCategoryDataPoint();
            point.setDate(entry.getKey());
            point.setCategoryName("Egresos");
            point.setAmount(entry.getValue());
            point.setTransactionType("WITHDRAWAL");
            result.add(point);
        }
        result.sort(Comparator.comparing(SpendingPatternResponse.TimeSeriesCategoryDataPoint::getDate));
        logger.info("Serie temporal final enviada al frontend:");
        for (SpendingPatternResponse.TimeSeriesCategoryDataPoint p : result) {
            logger.info("Fecha: {}, Categoria: {}, Monto: {}", p.getDate(), p.getCategoryName(), p.getAmount());
        }
        return result;
    }
}
