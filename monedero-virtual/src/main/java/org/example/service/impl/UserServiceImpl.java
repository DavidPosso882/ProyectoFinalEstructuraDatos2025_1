package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.dto.response.PointsAccountResponse;
import org.example.dto.response.UserResponse;
import org.example.dto.response.WalletResponse;
import org.example.model.PointsAccount;
import org.example.model.User;
import org.example.model.UserRank;
import org.example.repository.NotificationRepository;
import org.example.repository.PointsAccountRepository;
import org.example.repository.UserRepository;
import org.example.repository.WalletRepository;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PointsAccountRepository pointsAccountRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User saveUser(User user) {
        // Codificar la contraseña antes de guardar
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Cacheable(value = "userProfiles", key = "#userId")
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRank(user.getRank());
        response.setCreatedAt(user.getCreatedAt());

        // Obtener monederos
        List<WalletResponse> wallets = walletRepository.findByUser(user).stream()
                .map(wallet -> {
                    WalletResponse walletResponse = new WalletResponse();
                    walletResponse.setId(wallet.getId());
                    walletResponse.setName(wallet.getName());
                    walletResponse.setDescription(wallet.getDescription());
                    walletResponse.setBalance(wallet.getBalance());
                    walletResponse.setWalletType(wallet.getWalletType());
                    walletResponse.setCreatedAt(wallet.getCreatedAt());
                    walletResponse.setUpdatedAt(wallet.getUpdatedAt());
                    return walletResponse;
                })
                .collect(Collectors.toList());

        response.setWallets(wallets);

        // Obtener cuenta de puntos
        Optional<PointsAccount> pointsAccountOpt = pointsAccountRepository.findByUser(user);
        if (pointsAccountOpt.isPresent()) {
            PointsAccount pointsAccount = pointsAccountOpt.get();
            PointsAccountResponse pointsAccountResponse = new PointsAccountResponse();
            pointsAccountResponse.setId(pointsAccount.getId());
            pointsAccountResponse.setTotalPoints(pointsAccount.getTotalPoints());
            pointsAccountResponse.setAvailablePoints(pointsAccount.getAvailablePoints());
            pointsAccountResponse.setRedeemedPoints(pointsAccount.getRedeemedPoints());
            pointsAccountResponse.setCurrentRank(user.getRank());
            pointsAccountResponse.setPointsToNextRank(calculatePointsToNextRank(user.getRank(), pointsAccount.getTotalPoints()));
            pointsAccountResponse.setCreatedAt(pointsAccount.getCreatedAt());
            pointsAccountResponse.setUpdatedAt(pointsAccount.getUpdatedAt());

            response.setPointsAccount(pointsAccountResponse);
        }

        // Contar notificaciones no leídas
        long unreadCount = notificationRepository.countUnreadNotifications(userId);
        response.setUnreadNotificationsCount((int) unreadCount);

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
}