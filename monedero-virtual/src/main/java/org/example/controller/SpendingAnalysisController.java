package org.example.controller;

import org.example.dto.response.SpendingAnalysisResponse;
import org.example.dto.response.SpendingPatternResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.security.UserDetailsImpl;
import org.example.service.SpendingAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/analysis")
public class SpendingAnalysisController {
    
    @Autowired
    private SpendingAnalysisService analysisService;
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SpendingAnalysisResponse> analyzeUserSpending(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        return ResponseEntity.ok(analysisService.analyzeUserSpending(currentUser.getId(), startDate, endDate));
    }
    
    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SpendingAnalysisResponse> analyzeWalletSpending(
            @PathVariable Long walletId,
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        // Aquí se debería verificar que el usuario es propietario del monedero
        
        return ResponseEntity.ok(analysisService.analyzeWalletSpending(walletId, startDate, endDate));
    }
    
    @GetMapping("/user/month")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SpendingAnalysisResponse> analyzeUserSpendingLastMonth(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        
        return ResponseEntity.ok(analysisService.analyzeUserSpendingLastMonth(currentUser.getId()));
    }
    
    @GetMapping("/user/year")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SpendingAnalysisResponse> analyzeUserSpendingLastYear(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        
        return ResponseEntity.ok(analysisService.analyzeUserSpendingLastYear(currentUser.getId()));
    }
    
    @GetMapping("/user/compare")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SpendingAnalysisResponse> compareUserSpending(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime currentStartDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime currentEndDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime previousStartDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime previousEndDate) {
        
        return ResponseEntity.ok(analysisService.compareUserSpending(
                currentUser.getId(), currentStartDate, currentEndDate, previousStartDate, previousEndDate));
    }
    
    @GetMapping("/user/pattern")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SpendingPatternResponse> analyzeUserSpendingPattern(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(analysisService.analyzeUserSpendingPatterns(currentUser.getId(), startDate, endDate));
    }
}
