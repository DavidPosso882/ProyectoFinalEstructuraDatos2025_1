package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.WalletRequest;
import org.example.dto.response.WalletResponse;
import org.example.security.UserDetailsImpl;
import org.example.model.Wallet;
import org.example.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<WalletResponse>> getUserWallets(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String currency) {

        List<WalletResponse> wallets;
        if (currency != null && !currency.isEmpty()) {
            wallets = walletService.getUserWalletsByCurrency(userDetails.getId(), currency);
        } else {
            wallets = walletService.getUserWallets(userDetails.getId());
        }

        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<WalletResponse> getWalletById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        WalletResponse wallet = walletService.getWalletById(id, userDetails.getId());
        return ResponseEntity.ok(wallet);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<WalletResponse> createWallet(
            @Valid @RequestBody WalletRequest walletRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        WalletResponse createdWallet;
        if (walletRequest.getCurrencyCode() != null && !walletRequest.getCurrencyCode().isEmpty()) {
            createdWallet = walletService.createWallet(walletRequest, userDetails.getId(), walletRequest.getCurrencyCode());
        } else {
            createdWallet = walletService.createWallet(walletRequest, userDetails.getId());
        }

        return new ResponseEntity<>(createdWallet, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWallet(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        walletService.deleteWallet(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<WalletResponse> transferBetweenWallets(
            @RequestParam Long sourceWalletId,
            @RequestParam Long targetWalletId,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // Realizar la transferencia (solo validar que el usuario sea dueño del origen)
        Wallet updatedWallet = walletService.transferBetweenWallets(sourceWalletId, targetWalletId, amount, userDetails.getId());

        // Convertir y devolver el monedero actualizado
        return ResponseEntity.ok(walletService.convertToWalletResponse(updatedWallet));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<WalletResponse> updateWallet(
            @PathVariable Long id,
            @Valid @RequestBody WalletRequest walletRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        WalletResponse updatedWallet = walletService.updateWallet(id, walletRequest, userDetails.getId());
        return ResponseEntity.ok(updatedWallet);
    }

    @PostMapping("/transfer-to-user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<WalletResponse> transferToUser(
            @RequestParam Long sourceWalletId,
            @RequestParam Long targetUserId,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Wallet updatedWallet = walletService.transferToUser(sourceWalletId, targetUserId, amount, userDetails.getId());
        return ResponseEntity.ok(walletService.convertToWalletResponse(updatedWallet));
    }
}
