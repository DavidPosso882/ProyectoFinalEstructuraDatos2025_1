package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.CurrencyRequest;
import org.example.dto.response.CurrencyResponse;
import org.example.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para operaciones relacionadas con monedas.
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {
    
    @Autowired
    private CurrencyService currencyService;
    
    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies() {
        List<CurrencyResponse> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CurrencyResponse> getCurrencyById(@PathVariable Long id) {
        CurrencyResponse currency = currencyService.getCurrencyById(id);
        return ResponseEntity.ok(currency);
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<CurrencyResponse> getCurrencyByCode(@PathVariable String code) {
        CurrencyResponse currency = currencyService.getCurrencyByCode(code);
        return ResponseEntity.ok(currency);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurrencyResponse> createCurrency(@Valid @RequestBody CurrencyRequest currencyRequest) {
        CurrencyResponse createdCurrency = currencyService.createCurrency(currencyRequest);
        return new ResponseEntity<>(createdCurrency, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurrencyResponse> updateCurrency(
            @PathVariable Long id,
            @Valid @RequestBody CurrencyRequest currencyRequest) {
        CurrencyResponse updatedCurrency = currencyService.updateCurrency(id, currencyRequest);
        return ResponseEntity.ok(updatedCurrency);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCurrency(@PathVariable Long id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/base")
    public ResponseEntity<CurrencyResponse> getBaseCurrency() {
        CurrencyResponse baseCurrency = currencyService.convertToCurrencyResponse(currencyService.getBaseCurrency());
        return ResponseEntity.ok(baseCurrency);
    }
    
    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertCurrency(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to) {
        BigDecimal convertedAmount = currencyService.convertCurrency(amount, from, to);
        
        Map<String, Object> response = new HashMap<>();
        response.put("from", from);
        response.put("to", to);
        response.put("amount", amount);
        response.put("convertedAmount", convertedAmount);
        
        return ResponseEntity.ok(response);
    }
}
