package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.TransactionRequest;
import org.example.dto.response.TransactionResponse;
import org.example.security.UserDetailsImpl;
import org.example.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.example.service.impl.TransactionHistoryManager;
import org.example.model.User;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionHistoryManager transactionHistoryManager;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest transactionRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TransactionResponse transaction = transactionService.processTransaction(transactionRequest, userDetails.getId());
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getUserTransactions(userDetails.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TransactionResponse transaction = transactionService.getTransactionById(id, userDetails.getId());
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getWalletTransactions(
            @PathVariable Long walletId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getWalletTransactions(walletId, userDetails.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TransactionResponse reversedTransaction = transactionService.reverseTransaction(id, userDetails.getId());
        return ResponseEntity.ok(reversedTransaction);
    }

    @PostMapping("/undo-last")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> undoLastTransaction(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("No autenticado");
        var lastTx = transactionHistoryManager.popLastTransaction(userDetails.getId());
        if (lastTx == null) return ResponseEntity.badRequest().body("No hay transacción para deshacer");
        TransactionResponse reversed = transactionService.reverseTransaction(lastTx.getId(), userDetails.getId());
        return ResponseEntity.ok(reversed);
    }
}
