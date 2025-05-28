package org.example.controller;

import org.example.dto.response.UserResponse;
import org.example.model.User;
import org.example.security.UserDetailsImpl;
import org.example.service.UserService;
import org.example.service.impl.UserRankManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRankManager userRankManager;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponse userResponse = userService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserProfile(id);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<UserResponse>> getUserRanking(@RequestParam(defaultValue = "10") int limit) {
        var topUsers = userRankManager.getTopUsers(limit);
        List<UserResponse> response = topUsers.stream()
            .map(user -> userService.getUserProfile(user.getId()))
            .toList();
        return ResponseEntity.ok(response);
    }
}
