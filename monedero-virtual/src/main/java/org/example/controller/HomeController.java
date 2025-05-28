package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Monedero Virtual API is running!";
    }

    @GetMapping("/api/public/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Monedero Virtual con Sistema de Puntos");
        info.put("version", "1.0.0");
        info.put("description", "API para gestionar monederos virtuales con sistema de puntos");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("auth", "/api/auth");
        endpoints.put("users", "/api/users");
        endpoints.put("wallets", "/api/wallets");
        endpoints.put("transactions", "/api/transactions");
        endpoints.put("scheduled-transactions", "/api/scheduled-transactions");
        endpoints.put("points", "/api/points");
        endpoints.put("notifications", "/api/notifications");

        info.put("endpoints", endpoints);

        return info;
    }

    @GetMapping("/api/public/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        return status;
    }
}
