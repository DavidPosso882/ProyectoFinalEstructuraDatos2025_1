package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.PointsRedemptionRequest;
import org.example.dto.response.PointsAccountResponse;
import org.example.dto.response.PointsTransactionResponse;
import org.example.security.UserDetailsImpl;
import org.example.service.PointsService;
import org.example.service.impl.UserRankManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private UserRankManager userRankManager;

    @GetMapping("/account")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PointsAccountResponse> getUserPointsAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PointsAccountResponse pointsAccount = pointsService.getUserPointsAccount(userDetails.getId());
        return ResponseEntity.ok(pointsAccount);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PointsTransactionResponse>> getUserPointsTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {
        Page<PointsTransactionResponse> transactions = pointsService.getUserPointsTransactions(userDetails.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/redeem")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PointsTransactionResponse> redeemPoints(
            @Valid @RequestBody PointsRedemptionRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PointsTransactionResponse transaction = pointsService.redeemPoints(request, userDetails.getId());
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/benefits")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getAvailableBenefits(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(pointsService.getAvailableBenefits(userDetails.getId()));
    }

    @PostMapping("/update-ranks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateAllUserRanks() {
        userRankManager.recalculateAllRanks();
        return ResponseEntity.ok("Rangos actualizados correctamente");
    }
}
