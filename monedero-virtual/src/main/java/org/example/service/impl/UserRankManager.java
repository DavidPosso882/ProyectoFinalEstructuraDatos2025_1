package org.example.service.impl;

import jakarta.annotation.PostConstruct;
import org.example.datastructure.tree.AVLTree;
import org.example.model.User;
import org.example.model.UserRank;
import org.example.repository.UserRepository;
import org.example.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de rangos de usuario utilizando árboles AVL
 */
@Component
public class UserRankManager {

    private static final Logger logger = LoggerFactory.getLogger(UserRankManager.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    // Árbol AVL para gestionar usuarios por puntos
    private AVLTree<UserRankNode> userRankTree;

    // Mapa para acceso rápido a nodos por ID de usuario
    private Map<Long, UserRankNode> userNodeMap;

    @PostConstruct
    public void init() {
        userRankTree = new AVLTree<>();
        userNodeMap = new HashMap<>();

        // Cargar usuarios existentes
        loadAllUsers();
    }

    /**
     * Carga todos los usuarios en el árbol
     */
    private void loadAllUsers() {
        List<User> allUsers = userRepository.findAll();

        logger.info("Cargando {} usuarios en el árbol de rangos", allUsers.size());

        for (User user : allUsers) {
            // Asegurarse de que el usuario tenga un valor para points
            if (user.getPoints() < 0) {
                user.setPoints(0);
                userRepository.save(user);
            }
            addUserToTree(user);
        }
    }

    /**
     * Añade un usuario al árbol
     * @param user Usuario a añadir
     */
    public void addUserToTree(User user) {
        if (user == null || userNodeMap.containsKey(user.getId())) {
            return;
        }

        UserRankNode node = new UserRankNode(user);
        userRankTree.insert(node);
        userNodeMap.put(user.getId(), node);
    }

    /**
     * Actualiza los puntos de un usuario y recalcula su rango
     * @param userId ID del usuario
     * @param newPoints Nuevos puntos totales
     * @return true si el rango cambió, false en caso contrario
     */
    @Transactional
    public boolean updateUserPoints(Long userId, int newPoints) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));

        // Obtener el nodo actual o crear uno nuevo
        UserRankNode node = userNodeMap.get(userId);
        boolean isNewNode = false;

        if (node == null) {
            node = new UserRankNode(user);
            isNewNode = true;
        } else {
            // Eliminar el nodo actual del árbol para reinsertarlo con los nuevos puntos
            userRankTree.remove(node);
        }

        // Guardar el rango anterior
        UserRank oldRank = node.getRank();

        // Actualizar puntos
        node.setPoints(newPoints);

        // Recalcular rango
        UserRank newRank = calculateRank(newPoints);
        node.setRank(newRank);

        // Reinsertar en el árbol
        userRankTree.insert(node);
        userNodeMap.put(userId, node);

        // Actualizar en la base de datos si el rango cambió
        boolean rankChanged = oldRank != newRank;
        if (rankChanged || isNewNode) {
            user.setPoints(newPoints);
            user.setRank(newRank);
            userRepository.save(user);
        }

        return rankChanged;
    }

    /**
     * Calcula el rango basado en los puntos
     * @param points Puntos del usuario
     * @return Rango correspondiente
     */
    public UserRank calculateRank(int points) {
        // Usar el método estático del enum UserRank para consistencia
        return UserRank.getRankByPoints(points);
    }

    /**
     * Obtiene los usuarios de un rango específico
     * @param rank Rango a buscar
     * @param limit Número máximo de usuarios a devolver
     * @return Lista de usuarios del rango especificado
     */
    public List<User> getUsersByRank(UserRank rank, int limit) {
        List<UserRankNode> allNodes = userRankTree.toSortedList();

        return allNodes.stream()
            .filter(node -> node.getRank() == rank)
            .limit(limit)
            .map(UserRankNode::getUser)
            .toList();
    }

    /**
     * Obtiene los usuarios con más puntos
     * @param limit Número máximo de usuarios a devolver
     * @return Lista de usuarios ordenados por puntos (descendente)
     */
    public List<User> getTopUsers(int limit) {
        List<UserRankNode> allNodes = userRankTree.toSortedList();

        // Los nodos ya están ordenados por puntos (ascendente)
        // Invertir el orden para obtener los de mayor puntuación primero
        return allNodes.stream()
            .sorted((n1, n2) -> Integer.compare(n2.getPoints(), n1.getPoints()))
            .limit(limit)
            .map(UserRankNode::getUser)
            .toList();
    }

    /**
     * Tarea programada para recalcular rangos de todos los usuarios
     */
    @Scheduled(cron = "0 0 2 * * *") // Ejecutar a las 2 AM todos los días
    @Transactional
    public void recalculateAllRanks() {
        logger.info("Recalculando rangos de todos los usuarios");

        List<User> allUsers = userRepository.findAll();
        int updatedCount = 0;

        for (User user : allUsers) {
            UserRank oldRank = user.getRank();
            UserRank newRank = calculateRank(user.getPoints());

            if (oldRank != newRank) {
                user.setRank(newRank);
                userRepository.save(user);

                // Actualizar en el árbol
                UserRankNode node = userNodeMap.get(user.getId());
                if (node != null) {
                    userRankTree.remove(node);
                    node.setRank(newRank);
                    userRankTree.insert(node);
                }

                // Notificar al usuario
                notificationService.createNotification(
                    user.getId(),
                    "¡Has subido de rango!",
                    "Felicidades, ahora eres " + newRank.getDescription(),
                    org.example.model.NotificationType.RANK_CHANGED,
                    null,
                    org.example.model.Notification.RelatedEntityType.USER
                );

                updatedCount++;
            }
        }

        logger.info("Se actualizaron los rangos de {} usuarios", updatedCount);
    }

    /**
     * Clase interna para representar un nodo de usuario en el árbol
     * Implementa Comparable para poder usarse en el árbol AVL
     */
    public static class UserRankNode implements Comparable<UserRankNode> {
        private User user;
        private int points;
        private UserRank rank;

        public UserRankNode(User user) {
            this.user = user;
            this.points = user.getPoints();
            this.rank = user.getRank();
        }

        public User getUser() {
            return user;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public UserRank getRank() {
            return rank;
        }

        public void setRank(UserRank rank) {
            this.rank = rank;
        }

        @Override
        public int compareTo(UserRankNode other) {
            // Ordenar por puntos (ascendente)
            return Integer.compare(this.points, other.points);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            UserRankNode that = (UserRankNode) obj;
            return user.getId().equals(that.user.getId());
        }

        @Override
        public int hashCode() {
            return user.getId().hashCode();
        }
    }
}
