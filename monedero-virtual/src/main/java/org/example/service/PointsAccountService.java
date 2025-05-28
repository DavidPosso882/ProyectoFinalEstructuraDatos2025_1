package org.example.service;

import org.example.model.PointsAccount;
import org.example.model.User;
import org.example.model.UserRank;

import java.util.List;

public interface PointsAccountService {

    /**
     * Añade puntos a la cuenta de un usuario
     * @param userId ID del usuario
     * @param points Cantidad de puntos a añadir
     * @param description Descripción de la transacción
     * @param transactionId ID de la transacción relacionada (opcional)
     * @return Cuenta de puntos actualizada
     */
    PointsAccount addPoints(Long userId, int points, String description, Long transactionId);

    /**
     * Canjea puntos de la cuenta de un usuario
     * @param userId ID del usuario
     * @param points Cantidad de puntos a canjear
     * @param description Descripción del canje
     * @return Cuenta de puntos actualizada
     */
    PointsAccount redeemPoints(Long userId, int points, String description);

    /**
     * Ajusta los puntos de la cuenta de un usuario (para correcciones)
     * @param userId ID del usuario
     * @param points Cantidad de puntos a ajustar (positivo o negativo)
     * @param description Descripción del ajuste
     * @return Cuenta de puntos actualizada
     */
    PointsAccount adjustPoints(Long userId, int points, String description);

    /**
     * Obtiene la cuenta de puntos de un usuario
     * @param userId ID del usuario
     * @return Cuenta de puntos
     */
    PointsAccount getUserPointsAccount(Long userId);

    /**
     * Guarda una cuenta de puntos
     * @param pointsAccount Cuenta de puntos a guardar
     * @return Cuenta de puntos guardada
     */
    PointsAccount savePointsAccount(PointsAccount pointsAccount);

    /**
     * Obtiene los usuarios con más puntos
     * @param limit Número máximo de usuarios a devolver
     * @return Lista de usuarios ordenados por puntos (descendente)
     */
    List<User> getTopPointsUsers(int limit);

    /**
     * Obtiene los usuarios de un rango específico
     * @param rank Rango a buscar
     * @param limit Número máximo de usuarios a devolver
     * @return Lista de usuarios del rango especificado
     */
    List<User> getUsersByRank(UserRank rank, int limit);
}
