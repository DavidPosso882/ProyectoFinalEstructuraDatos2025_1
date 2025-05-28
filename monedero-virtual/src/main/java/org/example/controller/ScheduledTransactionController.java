package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.ScheduledTransactionRequest;
import org.example.dto.response.ScheduledTransactionResponse;
import org.example.security.UserDetailsImpl;
import org.example.service.ScheduledTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/scheduled-transactions")
public class ScheduledTransactionController {

    @Autowired
    private ScheduledTransactionService scheduledTransactionService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ScheduledTransactionResponse> createScheduledTransaction(
            @Valid @RequestBody ScheduledTransactionRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ScheduledTransactionResponse transaction = scheduledTransactionService.createScheduledTransaction(request, userDetails.getId());
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ScheduledTransactionResponse>> getUserScheduledTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ScheduledTransactionResponse> transactions = scheduledTransactionService.getUserScheduledTransactions(userDetails.getId());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ScheduledTransactionResponse> getScheduledTransactionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ScheduledTransactionResponse transaction = scheduledTransactionService.getScheduledTransactionById(id, userDetails.getId());
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelScheduledTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        scheduledTransactionService.cancelScheduledTransaction(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
