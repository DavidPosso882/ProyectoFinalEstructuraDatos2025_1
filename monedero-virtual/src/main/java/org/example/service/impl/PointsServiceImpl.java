package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.datastructure.tree.BinarySearchTree;
import org.example.dto.request.PointsRedemptionRequest;
import org.example.dto.response.PointsAccountResponse;
import org.example.dto.response.PointsTransactionResponse;
import org.example.model.*;
import org.example.repository.PointsAccountRepository;
import org.example.repository.PointsTransactionRepository;
import org.example.repository.UserRepository;
import org.example.service.NotificationService;
import org.example.service.PointsAccountService;
import org.example.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointsServiceImpl implements PointsService {

    @Autowired
    private PointsAccountRepository pointsAccountRepository;

    @Autowired
    private PointsTransactionRepository pointsTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointsAccountService pointsAccountService;

    @Autowired
    private NotificationService notificationService;

    // Árbol binario de búsqueda para gestionar los beneficios
    private BinarySearchTree<Benefit> benefitsTree;

    @PostConstruct
    public void init() {
        // Inicializar el árbol de beneficios
        benefitsTree = new BinarySearchTree<>();

        // Añadir beneficios predefinidos al árbol
        for (Map<String, Object> benefitMap : getAllBenefits()) {
            Benefit benefit = new Benefit(
                (String) benefitMap.get("code"),
                (String) benefitMap.get("name"),
                (String) benefitMap.get("description"),
                (Integer) benefitMap.get("points")
            );
            benefitsTree.insert(benefit);
        }
    }

    @Override
    @Cacheable(value = "userPointsAccount", key = "#userId")
    public PointsAccountResponse getUserPointsAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        PointsAccount pointsAccount = pointsAccountRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta de puntos no encontrada"));

        return convertToPointsAccountResponse(pointsAccount);
    }

    @Override
    public Page<PointsTransactionResponse> getUserPointsTransactions(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        PointsAccount pointsAccount = pointsAccountRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta de puntos no encontrada"));

        // Filtrar solo transacciones de canje (REDEEMED) para el historial
        return pointsTransactionRepository.findByPointsAccountAndTypeOrderByTransactionDateDesc(
                pointsAccount, PointsTransactionType.REDEEMED, pageable)
                .map(this::convertToPointsTransactionResponse);
    }

    @Override
    @Transactional
    public PointsTransactionResponse redeemPoints(PointsRedemptionRequest request, Long userId) {
        // Validar beneficio
        Map<String, Object> benefit = getBenefitByCode(request.getBenefitCode());
        if (benefit == null) {
            throw new IllegalArgumentException("Beneficio no encontrado");
        }

        int requiredPoints = (int) benefit.get("points");
        if (request.getPoints() < requiredPoints) {
            throw new IllegalArgumentException("Puntos insuficientes para este beneficio");
        }

        // Canjear puntos
        String description = "Canje de " + request.getPoints() + " puntos por " + benefit.get("name");
        PointsAccount pointsAccount = pointsAccountService.redeemPoints(userId, request.getPoints(), description);

        // Buscar la última transacción de puntos (la que acabamos de crear)
        PointsTransaction pointsTransaction = pointsTransactionRepository.findByPointsAccountOrderByTransactionDateDesc(pointsAccount, Pageable.ofSize(1))
                .getContent().get(0);

        // Crear notificación
        notificationService.createNotification(
                userId,
                "Puntos Canjeados",
                "Has canjeado " + request.getPoints() + " puntos por " + benefit.get("name"),
                NotificationType.POINTS_EARNED,
                pointsTransaction.getId(),
                Notification.RelatedEntityType.POINTS_TRANSACTION);

        return convertToPointsTransactionResponse(pointsTransaction);
    }

    @Override
    @Cacheable(value = "availableBenefits", key = "#userId")
    public Map<String, Object> getAvailableBenefits(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        PointsAccount pointsAccount = pointsAccountRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta de puntos no encontrada"));

        Map<String, Object> result = new HashMap<>();
        result.put("availablePoints", pointsAccount.getAvailablePoints());
        result.put("benefits", getAllBenefits());

        return result;
    }

    private Map<String, Object> getBenefitByCode(String code) {
        // Buscar el beneficio en el árbol binario
        Benefit searchKey = new Benefit(code, "", "", 0);
        Benefit foundBenefit = benefitsTree.find(searchKey);

        if (foundBenefit == null) {
            return null;
        }

        // Convertir a Map para mantener compatibilidad con el código existente
        Map<String, Object> benefitMap = new HashMap<>();
        benefitMap.put("code", foundBenefit.getCode());
        benefitMap.put("name", foundBenefit.getName());
        benefitMap.put("description", foundBenefit.getDescription());
        benefitMap.put("points", foundBenefit.getRequiredPoints());

        return benefitMap;
    }

    private Map<String, Object>[] getAllBenefits() {
        // Beneficios predefinidos
        Map<String, Object> benefit1 = new HashMap<>();
        benefit1.put("code", "TRANSFER_DISCOUNT");
        benefit1.put("name", "Reducción del 10% en comisión por transferencias");
        benefit1.put("description", "Obtén un 10% de descuento en las comisiones por transferencias durante un mes");
        benefit1.put("points", 100);

        Map<String, Object> benefit2 = new HashMap<>();
        benefit2.put("code", "FREE_WITHDRAWALS");
        benefit2.put("name", "Un mes sin cargos por retiros");
        benefit2.put("description", "No pagarás comisiones por retiros durante un mes");
        benefit2.put("points", 500);

        Map<String, Object> benefit3 = new HashMap<>();
        benefit3.put("code", "BALANCE_BONUS");
        benefit3.put("name", "Bono de saldo de 50 unidades");
        benefit3.put("description", "Recibe un bono de 50 unidades en tu monedero principal");
        benefit3.put("points", 1000);

        // Nuevos beneficios para rangos superiores
        Map<String, Object> benefit4 = new HashMap<>();
        benefit4.put("code", "PREMIUM_SUPPORT");
        benefit4.put("name", "Soporte premium prioritario");
        benefit4.put("description", "Acceso a soporte prioritario durante 3 meses");
        benefit4.put("points", 2000);

        Map<String, Object> benefit5 = new HashMap<>();
        benefit5.put("code", "CASHBACK_BOOST");
        benefit5.put("name", "Aumento de cashback en compras");
        benefit5.put("description", "Recibe un 5% adicional de cashback en todas tus compras durante 2 meses");
        benefit5.put("points", 3000);

        return new Map[]{benefit1, benefit2, benefit3, benefit4, benefit5};
    }

    /**
     * Obtiene todos los beneficios disponibles como una lista
     * @return Lista de beneficios
     */
    public List<Benefit> getAllBenefitsAsList() {
        if (benefitsTree == null) {
            init(); // Asegurar que el árbol está inicializado
        }
        return benefitsTree.toSortedList();
    }

    private PointsAccountResponse convertToPointsAccountResponse(PointsAccount pointsAccount) {
        PointsAccountResponse response = new PointsAccountResponse();
        response.setId(pointsAccount.getId());
        response.setTotalPoints(pointsAccount.getTotalPoints());
        response.setAvailablePoints(pointsAccount.getAvailablePoints());
        response.setRedeemedPoints(pointsAccount.getRedeemedPoints());

        // Obtener el rango actual del usuario
        User user = pointsAccount.getUser();
        response.setCurrentRank(user.getRank());

        // Calcular puntos para el siguiente rango
        response.setPointsToNextRank(calculatePointsToNextRank(user.getRank(), pointsAccount.getTotalPoints()));

        response.setCreatedAt(pointsAccount.getCreatedAt());
        response.setUpdatedAt(pointsAccount.getUpdatedAt());
        return response;
    }

    /**
     * Calcula los puntos necesarios para alcanzar el siguiente rango
     * @param currentRank Rango actual del usuario
     * @param currentPoints Puntos actuales del usuario
     * @return Puntos necesarios para el siguiente rango
     */
    private Integer calculatePointsToNextRank(UserRank currentRank, Integer currentPoints) {
        UserRank nextRank = getNextRank(currentRank);
        if (nextRank == currentRank) {
            return 0; // Ya está en el rango máximo
        }
        return Math.max(0, nextRank.getMinPoints() - currentPoints);
    }

    /**
     * Obtiene el siguiente rango
     * @param currentRank Rango actual
     * @return Siguiente rango
     */
    private UserRank getNextRank(UserRank currentRank) {
        switch (currentRank) {
            case BRONZE:
                return UserRank.SILVER;
            case SILVER:
                return UserRank.GOLD;
            case GOLD:
                return UserRank.PLATINUM;
            case PLATINUM:
            default:
                return UserRank.PLATINUM; // Ya está en el rango máximo
        }
    }

    private PointsTransactionResponse convertToPointsTransactionResponse(PointsTransaction transaction) {
        PointsTransactionResponse response = new PointsTransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setRelatedTransactionId(transaction.getRelatedTransactionId());
        return response;
    }

    /**
     * Clase interna para representar un beneficio
     * Implementa Comparable para poder usarse en el árbol binario de búsqueda
     */
    public static class Benefit implements Comparable<Benefit> {
        private String code;
        private String name;
        private String description;
        private int requiredPoints;

        public Benefit(String code, String name, String description, int requiredPoints) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.requiredPoints = requiredPoints;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getRequiredPoints() {
            return requiredPoints;
        }

        @Override
        public int compareTo(Benefit other) {
            // Comparar por código para búsquedas eficientes
            return this.code.compareTo(other.code);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Benefit benefit = (Benefit) obj;
            return code.equals(benefit.code);
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }
    }
}
