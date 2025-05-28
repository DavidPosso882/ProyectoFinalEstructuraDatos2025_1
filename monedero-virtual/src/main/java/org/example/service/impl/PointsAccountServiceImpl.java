package org.example.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import jakarta.persistence.EntityNotFoundException;
import org.example.model.*;
import org.example.repository.PointsAccountRepository;
import org.example.repository.PointsTransactionRepository;
import org.example.repository.UserRepository;
import org.example.service.PointsAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PointsAccountServiceImpl implements PointsAccountService {

    private static final Logger logger = LoggerFactory.getLogger(PointsAccountServiceImpl.class);

    @Autowired
    private PointsAccountRepository pointsAccountRepository;

    @Autowired
    private PointsTransactionRepository pointsTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private HazelcastTransactionManager transactionManager;

    @Autowired
    private UserRankManager userRankManager;

    @Override
    @Transactional
    @CacheEvict(value = {"userPoints", "userRanks", "userPointsAccount"}, key = "#userId")
    public PointsAccount addPoints(Long userId, int points, String description, Long transactionId) {
        if (points <= 0) {
            throw new IllegalArgumentException("La cantidad de puntos debe ser positiva");
        }

        PointsAccount pointsAccount = getUserPointsAccount(userId);

        // Crear transacción de puntos
        PointsTransaction pointsTransaction = new PointsTransaction();
        pointsTransaction.setPointsAccount(pointsAccount);
        pointsTransaction.setAmount(points);
        pointsTransaction.setType(PointsTransactionType.EARNED);
        pointsTransaction.setDescription(description);
        pointsTransaction.setRelatedTransactionId(transactionId);

        pointsTransactionRepository.save(pointsTransaction);

        // Actualizar cuenta de puntos
        pointsAccount.setTotalPoints(pointsAccount.getTotalPoints() + points);
        pointsAccount.setAvailablePoints(pointsAccount.getAvailablePoints() + points);

        // Actualizar rango del usuario usando el gestor de rangos
        User user = pointsAccount.getUser();
        boolean rankChanged = userRankManager.updateUserPoints(userId, pointsAccount.getTotalPoints());

        if (rankChanged) {
            logger.info("Usuario {} ha cambiado de rango a {}", userId, user.getRank());
        }

        // Actualizar puntos en Hazelcast
        IMap<String, Integer> userPoints = hazelcastInstance.getMap("userPoints");
        userPoints.put("user:" + userId, pointsAccount.getTotalPoints());

        // Actualizar rango en Hazelcast
        IMap<String, String> userRanks = hazelcastInstance.getMap("userRanks");
        userRanks.put("user:" + userId, user.getRank().name());

        return pointsAccountRepository.save(pointsAccount);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userPoints", "userPointsAccount", "availableBenefits"}, key = "#userId")
    public PointsAccount redeemPoints(Long userId, int points, String description) {
        if (points <= 0) {
            throw new IllegalArgumentException("La cantidad de puntos debe ser positiva");
        }

        PointsAccount pointsAccount = getUserPointsAccount(userId);

        if (pointsAccount.getAvailablePoints() < points) {
            throw new IllegalStateException("No hay suficientes puntos disponibles para canjear");
        }

        // Crear transacción de puntos
        PointsTransaction pointsTransaction = new PointsTransaction();
        pointsTransaction.setPointsAccount(pointsAccount);
        pointsTransaction.setAmount(points);
        pointsTransaction.setType(PointsTransactionType.REDEEMED);
        pointsTransaction.setDescription(description);

        pointsTransactionRepository.save(pointsTransaction);

        // Actualizar cuenta de puntos
        pointsAccount.setAvailablePoints(pointsAccount.getAvailablePoints() - points);
        pointsAccount.setRedeemedPoints(pointsAccount.getRedeemedPoints() + points);

        return pointsAccountRepository.save(pointsAccount);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userPoints", "userRanks", "userPointsAccount"}, key = "#userId")
    public PointsAccount adjustPoints(Long userId, int points, String description) {
        PointsAccount pointsAccount = getUserPointsAccount(userId);

        // Crear transacción de puntos
        PointsTransaction pointsTransaction = new PointsTransaction();
        pointsTransaction.setPointsAccount(pointsAccount);
        pointsTransaction.setAmount(Math.abs(points));
        pointsTransaction.setType(PointsTransactionType.ADJUSTED);
        pointsTransaction.setDescription(description);

        pointsTransactionRepository.save(pointsTransaction);

        // Actualizar cuenta de puntos
        pointsAccount.setTotalPoints(pointsAccount.getTotalPoints() + points);
        pointsAccount.setAvailablePoints(pointsAccount.getAvailablePoints() + points);

        // Si los puntos disponibles son negativos, ajustar a cero
        if (pointsAccount.getAvailablePoints() < 0) {
            pointsAccount.setAvailablePoints(0);
        }

        // Actualizar rango del usuario usando el gestor de rangos
        User user = pointsAccount.getUser();
        boolean rankChanged = userRankManager.updateUserPoints(userId, pointsAccount.getTotalPoints());

        if (rankChanged) {
            logger.info("Usuario {} ha cambiado de rango a {}", userId, user.getRank());
        }

        return pointsAccountRepository.save(pointsAccount);
    }

    @Override
    @Cacheable(value = "userPoints", key = "#userId")
    public PointsAccount getUserPointsAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        return pointsAccountRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta de puntos no encontrada"));
    }

    @Override
    @Transactional
    public PointsAccount savePointsAccount(PointsAccount pointsAccount) {
        return pointsAccountRepository.save(pointsAccount);
    }

    /**
     * Obtiene los usuarios con más puntos
     * @param limit Número máximo de usuarios a devolver
     * @return Lista de usuarios ordenados por puntos (descendente)
     */
    @Override
    public List<User> getTopPointsUsers(int limit) {
        return userRankManager.getTopUsers(limit);
    }

    /**
     * Obtiene los usuarios de un rango específico
     * @param rank Rango a buscar
     * @param limit Número máximo de usuarios a devolver
     * @return Lista de usuarios del rango especificado
     */
    @Override
    public List<User> getUsersByRank(UserRank rank, int limit) {
        return userRankManager.getUsersByRank(rank, limit);
    }
}
