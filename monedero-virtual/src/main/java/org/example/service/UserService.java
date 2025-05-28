package org.example.service;

import org.example.dto.response.UserResponse;
import org.example.model.User;
import java.util.Optional;

public interface UserService {

    User saveUser(User user);

    Optional<User> findUserById(Long id);

    Optional<User> findByUsername(String username);

    /**
     * Obtiene el perfil completo de un usuario
     * @param userId ID del usuario
     * @return Perfil del usuario
     */
    UserResponse getUserProfile(Long userId);
}