package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.RewardCategoryRequest;
import org.example.dto.request.RewardRedemptionRequest;
import org.example.dto.request.RewardRequest;
import org.example.dto.response.ApiResponse;
import org.example.dto.response.RewardCategoryResponse;
import org.example.dto.response.RewardRedemptionResponse;
import org.example.dto.response.RewardResponse;
import org.example.security.CurrentUser;
import org.example.security.UserPrincipal;
import org.example.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {
    
    @Autowired
    private RewardService rewardService;
    
    // Endpoints para categorías de recompensas
    
    @GetMapping("/categories")
    public ResponseEntity<List<RewardCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(rewardService.getAllCategories());
    }
    
    @GetMapping("/categories/active")
    public ResponseEntity<List<RewardCategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok(rewardService.getActiveCategories());
    }
    
    @GetMapping("/categories/{id}")
    public ResponseEntity<RewardCategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(rewardService.getCategoryById(id));
    }
    
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardCategoryResponse> createCategory(
            @Valid @RequestBody RewardCategoryRequest request) {
        return ResponseEntity.ok(rewardService.createCategory(request));
    }
    
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardCategoryResponse> updateCategory(
            @PathVariable Long id, @Valid @RequestBody RewardCategoryRequest request) {
        return ResponseEntity.ok(rewardService.updateCategory(id, request));
    }
    
    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        rewardService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse(true, "Categoría eliminada correctamente"));
    }
    
    // Endpoints para recompensas
    
    @GetMapping
    public ResponseEntity<Page<RewardResponse>> getAllRewards(Pageable pageable) {
        return ResponseEntity.ok(rewardService.getAllRewards(pageable));
    }
    
    @GetMapping("/active")
    public ResponseEntity<Page<RewardResponse>> getActiveRewards(Pageable pageable) {
        return ResponseEntity.ok(rewardService.getActiveRewards(pageable));
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<RewardResponse>> getFeaturedRewards() {
        return ResponseEntity.ok(rewardService.getFeaturedRewards());
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<RewardResponse>> getRewardsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(rewardService.getRewardsByCategory(categoryId));
    }
    
    @GetMapping("/available")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<RewardResponse>> getAvailableRewardsForUser(
            @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(rewardService.getAvailableRewardsForUser(currentUser.getId()));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RewardResponse> getRewardById(@PathVariable Long id) {
        return ResponseEntity.ok(rewardService.getRewardById(id));
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<RewardResponse> getRewardByCode(@PathVariable String code) {
        return ResponseEntity.ok(rewardService.getRewardByCode(code));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardResponse> createReward(
            @Valid @RequestBody RewardRequest request) {
        return ResponseEntity.ok(rewardService.createReward(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardResponse> updateReward(
            @PathVariable Long id, @Valid @RequestBody RewardRequest request) {
        return ResponseEntity.ok(rewardService.updateReward(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteReward(@PathVariable Long id) {
        rewardService.deleteReward(id);
        return ResponseEntity.ok(new ApiResponse(true, "Recompensa eliminada correctamente"));
    }
    
    // Endpoints para canjes de recompensas
    
    @PostMapping("/redeem")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<RewardRedemptionResponse> redeemReward(
            @Valid @RequestBody RewardRedemptionRequest request,
            @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(rewardService.redeemReward(request, currentUser.getId()));
    }
    
    @GetMapping("/redemptions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<RewardRedemptionResponse>> getUserRedemptions(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        return ResponseEntity.ok(rewardService.getUserRedemptions(currentUser.getId(), pageable));
    }
    
    @GetMapping("/redemptions/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<RewardRedemptionResponse> getRedemptionById(@PathVariable Long id) {
        return ResponseEntity.ok(rewardService.getRedemptionById(id));
    }
    
    @GetMapping("/redemptions/code/{code}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<RewardRedemptionResponse> getRedemptionByCode(@PathVariable String code) {
        return ResponseEntity.ok(rewardService.getRedemptionByCode(code));
    }
    
    @PostMapping("/redemptions/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardRedemptionResponse> completeRedemption(@PathVariable Long id) {
        return ResponseEntity.ok(rewardService.completeRedemption(id));
    }
    
    @PostMapping("/redemptions/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardRedemptionResponse> cancelRedemption(
            @PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(rewardService.cancelRedemption(id, reason));
    }
    
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> initializeDefaultRewards() {
        rewardService.initializeDefaultRewards();
        return ResponseEntity.ok(new ApiResponse(true, "Recompensas predeterminadas inicializadas correctamente"));
    }
}
