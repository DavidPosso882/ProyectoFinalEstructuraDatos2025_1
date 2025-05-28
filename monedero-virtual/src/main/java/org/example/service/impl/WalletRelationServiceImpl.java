package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.datastructure.graph.DirectedGraph;
import org.example.datastructure.graph.Edge;
import org.example.dto.request.TransactionRequest;
import org.example.dto.request.WalletRelationRequest;
import org.example.dto.response.TransactionResponse;
import org.example.dto.response.WalletRelationResponse;
import org.example.dto.response.WalletResponse;
import org.example.model.*;
import org.example.repository.WalletRelationRepository;
import org.example.repository.WalletRepository;
import org.example.service.TransactionService;
import org.example.service.WalletRelationService;
import org.example.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WalletRelationServiceImpl implements WalletRelationService {

    private static final Logger logger = LoggerFactory.getLogger(WalletRelationServiceImpl.class);

    @Autowired
    private WalletRelationRepository walletRelationRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Override
    @Transactional
    public WalletRelationResponse createWalletRelation(WalletRelationRequest request, Long userId) {
        // Verificar que los monederos existen y pertenecen al usuario
        Wallet sourceWallet = walletRepository.findById(request.getSourceWalletId())
                .orElseThrow(() -> new EntityNotFoundException("Monedero origen no encontrado"));

        Wallet targetWallet = walletRepository.findById(request.getTargetWalletId())
                .orElseThrow(() -> new EntityNotFoundException("Monedero destino no encontrado"));

        if (!sourceWallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para crear relaciones desde este monedero");
        }

        if (!targetWallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para crear relaciones hacia este monedero");
        }

        // Verificar que no exista ya una relación entre estos monederos
        if (walletRelationRepository.existsRelationBetweenWallets(
                sourceWallet.getId(), targetWallet.getId())) {
            throw new IllegalStateException("Ya existe una relación entre estos monederos");
        }

        // Crear la relación
        WalletRelation relation = sourceWallet.addRelationTo(targetWallet, request.getRelationType());

        // Configurar propiedades adicionales
        relation.setAutoTransferPercentage(request.getAutoTransferPercentage());
        relation.setAutoTransferThreshold(request.getAutoTransferThreshold());
        relation.setAutoTransferEnabled(request.isAutoTransferEnabled());

        // Guardar la relación
        WalletRelation savedRelation = walletRelationRepository.save(relation);

        return convertToWalletRelationResponse(savedRelation);
    }

    @Override
    @Transactional
    public WalletRelationResponse updateWalletRelation(Long relationId, WalletRelationRequest request, Long userId) {
        WalletRelation relation = walletRelationRepository.findById(relationId)
                .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));

        // Verificar que el usuario es propietario de los monederos
        if (!relation.getSourceWallet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para actualizar esta relación");
        }

        // Actualizar propiedades
        relation.setRelationType(request.getRelationType());
        relation.setAutoTransferPercentage(request.getAutoTransferPercentage());
        relation.setAutoTransferThreshold(request.getAutoTransferThreshold());
        relation.setAutoTransferEnabled(request.isAutoTransferEnabled());

        // Guardar cambios
        WalletRelation updatedRelation = walletRelationRepository.save(relation);

        return convertToWalletRelationResponse(updatedRelation);
    }

    @Override
    @Transactional
    public void deleteWalletRelation(Long relationId, Long userId) {
        WalletRelation relation = walletRelationRepository.findById(relationId)
                .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));

        // Verificar que el usuario es propietario de los monederos
        if (!relation.getSourceWallet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta relación");
        }

        // Eliminar la relación
        relation.getSourceWallet().removeOutgoingRelation(relation);
        walletRelationRepository.delete(relation);
    }

    @Override
    public WalletRelationResponse getWalletRelation(Long relationId, Long userId) {
        WalletRelation relation = walletRelationRepository.findById(relationId)
                .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));

        // Verificar que el usuario es propietario de los monederos
        if (!relation.getSourceWallet().getUser().getId().equals(userId) &&
            !relation.getTargetWallet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para ver esta relación");
        }

        return convertToWalletRelationResponse(relation);
    }

    @Override
    public List<WalletRelationResponse> getUserWalletRelations(Long userId) {
        List<WalletRelation> relations = walletRelationRepository.findAllByUserId(userId);
        return relations.stream()
                .map(this::convertToWalletRelationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletRelationResponse> getWalletOutgoingRelations(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para ver las relaciones de este monedero");
        }

        List<WalletRelation> relations = walletRelationRepository.findBySourceWallet(wallet);
        return relations.stream()
                .map(this::convertToWalletRelationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletRelationResponse> getWalletIncomingRelations(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para ver las relaciones de este monedero");
        }

        List<WalletRelation> relations = walletRelationRepository.findByTargetWallet(wallet);
        return relations.stream()
                .map(this::convertToWalletRelationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletRelationResponse> getWalletRelationsByType(WalletRelationType relationType, Long userId) {
        List<WalletRelation> allUserRelations = walletRelationRepository.findAllByUserId(userId);

        return allUserRelations.stream()
                .filter(relation -> relation.getRelationType() == relationType)
                .map(this::convertToWalletRelationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WalletRelationResponse setAutoTransferEnabled(Long relationId, boolean enabled, Long userId) {
        WalletRelation relation = walletRelationRepository.findById(relationId)
                .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));

        if (!relation.getSourceWallet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para modificar esta relación");
        }

        relation.setAutoTransferEnabled(enabled);
        WalletRelation updatedRelation = walletRelationRepository.save(relation);

        return convertToWalletRelationResponse(updatedRelation);
    }

    @Override
    @Transactional
    public WalletRelationResponse setAutoTransferPercentage(Long relationId, BigDecimal percentage, Long userId) {
        WalletRelation relation = walletRelationRepository.findById(relationId)
                .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));

        if (!relation.getSourceWallet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para modificar esta relación");
        }

        if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }

        relation.setAutoTransferPercentage(percentage);
        WalletRelation updatedRelation = walletRelationRepository.save(relation);

        return convertToWalletRelationResponse(updatedRelation);
    }

    @Override
    @Transactional
    public WalletRelationResponse setAutoTransferThreshold(Long relationId, BigDecimal threshold, Long userId) {
        WalletRelation relation = walletRelationRepository.findById(relationId)
                .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));

        if (!relation.getSourceWallet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para modificar esta relación");
        }

        if (threshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El umbral debe ser mayor o igual a cero");
        }

        relation.setAutoTransferThreshold(threshold);
        WalletRelation updatedRelation = walletRelationRepository.save(relation);

        return convertToWalletRelationResponse(updatedRelation);
    }

    @Override
    @Transactional
    public List<WalletRelationResponse> processAutoTransfers(Long walletId, BigDecimal amount, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero no encontrado"));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para procesar transferencias automáticas en este monedero");
        }

        List<WalletRelation> outgoingRelations = walletRelationRepository.findBySourceWallet(wallet);
        List<WalletRelationResponse> processedRelations = new ArrayList<>();

        for (WalletRelation relation : outgoingRelations) {
            if (relation.canAutoTransfer()) {
                BigDecimal transferAmount = relation.calculateAutoTransferAmount(amount);

                if (transferAmount.compareTo(BigDecimal.ZERO) > 0 &&
                    wallet.hasSufficientBalance(transferAmount)) {

                    // Crear la transacción de transferencia
                    TransactionRequest transferRequest = new TransactionRequest();
                    transferRequest.setType(TransactionType.TRANSFER);
                    transferRequest.setAmount(transferAmount);
                    transferRequest.setSourceWalletId(wallet.getId());
                    transferRequest.setTargetWalletId(relation.getTargetWallet().getId());
                    transferRequest.setDescription("Transferencia automática - " + relation.getRelationType().getName());

                    TransactionResponse transaction = transactionService.processTransaction(transferRequest, userId);

                    logger.info("Transferencia automática procesada: {} -> {} por {}",
                        wallet.getName(), relation.getTargetWallet().getName(), transferAmount);

                    processedRelations.add(convertToWalletRelationResponse(relation));
                }
            }
        }

        return processedRelations;
    }

    @Override
    public boolean existsPathBetweenWallets(Long sourceWalletId, Long targetWalletId, Long userId) {
        // Construir el grafo de monederos del usuario
        DirectedGraph<Long> walletGraph = buildUserWalletGraph(userId);

        // Verificar si existe un camino entre los monederos
        List<Long> path = walletGraph.breadthFirstTraversal(sourceWalletId);
        return path.contains(targetWalletId);
    }

    @Override
    public List<WalletRelationResponse> findShortestPath(Long sourceWalletId, Long targetWalletId, Long userId) {
        // Verificar que los monederos existen y pertenecen al usuario
        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero origen no encontrado"));

        Wallet targetWallet = walletRepository.findById(targetWalletId)
                .orElseThrow(() -> new EntityNotFoundException("Monedero destino no encontrado"));

        if (!sourceWallet.getUser().getId().equals(userId) || !targetWallet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para acceder a estos monederos");
        }

        // Construir el grafo de monederos del usuario
        DirectedGraph<Long> walletGraph = buildUserWalletGraph(userId);

        // Implementar algoritmo de Dijkstra para encontrar el camino más corto
        Map<Long, Long> previous = new HashMap<>();
        Map<Long, Double> distances = new HashMap<>();
        PriorityQueue<Long> queue = new PriorityQueue<>(
            Comparator.comparingDouble(distances::get));

        // Inicializar distancias
        for (Long walletId : walletGraph.getVertices()) {
            distances.put(walletId, Double.POSITIVE_INFINITY);
        }
        distances.put(sourceWalletId, 0.0);
        queue.add(sourceWalletId);

        // Algoritmo de Dijkstra
        while (!queue.isEmpty()) {
            Long current = queue.poll();

            if (current.equals(targetWalletId)) {
                break; // Llegamos al destino
            }

            double currentDistance = distances.get(current);

            for (Edge<Long> edge : walletGraph.getEdges(current)) {
                Long neighbor = edge.getDestination();
                double weight = edge.getWeight();
                double distance = currentDistance + weight;

                if (distance < distances.get(neighbor)) {
                    distances.put(neighbor, distance);
                    previous.put(neighbor, current);

                    // Actualizar la cola
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // Reconstruir el camino
        List<Long> path = new ArrayList<>();
        Long current = targetWalletId;

        if (!previous.containsKey(current)) {
            return new ArrayList<>(); // No hay camino
        }

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        // Convertir el camino a relaciones
        List<WalletRelationResponse> relationPath = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            Long currentWalletId = path.get(i);
            Long nextWalletId = path.get(i + 1);

            Optional<WalletRelation> relation = walletRelationRepository.findBySourceWalletAndTargetWallet(
                walletRepository.findById(currentWalletId).get(),
                walletRepository.findById(nextWalletId).get());

            if (relation.isPresent()) {
                relationPath.add(convertToWalletRelationResponse(relation.get()));
            }
        }

        return relationPath;
    }

    /**
     * Construye un grafo dirigido de los monederos de un usuario
     * @param userId ID del usuario
     * @return Grafo de monederos
     */
    private DirectedGraph<Long> buildUserWalletGraph(Long userId) {
        DirectedGraph<Long> graph = new DirectedGraph<>();

        // Obtener todos los monederos del usuario
        List<Wallet> userWallets = walletRepository.findByUserId(userId);

        // Añadir vértices al grafo
        for (Wallet wallet : userWallets) {
            graph.addVertex(wallet.getId());
        }

        // Obtener todas las relaciones del usuario
        List<WalletRelation> userRelations = walletRelationRepository.findAllByUserId(userId);

        // Añadir aristas al grafo
        for (WalletRelation relation : userRelations) {
            // Solo considerar relaciones entre monederos del mismo usuario
            if (relation.getSourceWallet().getUser().getId().equals(userId) &&
                relation.getTargetWallet().getUser().getId().equals(userId)) {

                graph.addEdge(
                    relation.getSourceWallet().getId(),
                    relation.getTargetWallet().getId(),
                    1.0); // Peso uniforme para camino más corto por número de saltos
            }
        }

        return graph;
    }

    /**
     * Convierte una entidad WalletRelation a su DTO de respuesta
     * @param relation Relación a convertir
     * @return DTO de respuesta
     */
    private WalletRelationResponse convertToWalletRelationResponse(WalletRelation relation) {
        WalletRelationResponse response = new WalletRelationResponse();
        response.setId(relation.getId());
        response.setSourceWallet(walletService.convertToWalletResponse(relation.getSourceWallet()));
        response.setTargetWallet(walletService.convertToWalletResponse(relation.getTargetWallet()));
        response.setRelationType(relation.getRelationType());
        response.setRelationTypeName(relation.getRelationType().getName());
        response.setRelationTypeDescription(relation.getRelationType().getDescription());
        response.setAutoTransferPercentage(relation.getAutoTransferPercentage());
        response.setAutoTransferThreshold(relation.getAutoTransferThreshold());
        response.setAutoTransferEnabled(relation.isAutoTransferEnabled());
        response.setCreatedAt(relation.getCreatedAt());
        response.setUpdatedAt(relation.getUpdatedAt());
        return response;
    }
}
