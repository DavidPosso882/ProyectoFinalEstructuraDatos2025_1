package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.example.dto.request.CurrencyRequest;
import org.example.dto.response.CurrencyResponse;
import org.example.model.Currency;
import org.example.repository.CurrencyRepository;
import org.example.service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de monedas.
 */
@Service
public class CurrencyServiceImpl implements CurrencyService {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);
    
    @Autowired
    private CurrencyRepository currencyRepository;
    
    @Override
    @Cacheable("currencies")
    public List<CurrencyResponse> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(this::convertToCurrencyResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "currency", key = "#id")
    public CurrencyResponse getCurrencyById(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Moneda no encontrada con ID: " + id));
        return convertToCurrencyResponse(currency);
    }
    
    @Override
    @Cacheable(value = "currency", key = "#code")
    public CurrencyResponse getCurrencyByCode(String code) {
        Currency currency = currencyRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Moneda no encontrada con código: " + code));
        return convertToCurrencyResponse(currency);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"currencies", "currency"}, allEntries = true)
    public CurrencyResponse createCurrency(CurrencyRequest currencyRequest) {
        // Verificar si ya existe una moneda con el mismo código
        if (currencyRepository.existsByCode(currencyRequest.getCode())) {
            throw new IllegalArgumentException("Ya existe una moneda con el código: " + currencyRequest.getCode());
        }
        
        // Si es moneda base, verificar que no haya otra moneda base
        if (currencyRequest.isBaseCurrency() && currencyRepository.countBaseCurrencies() > 0) {
            throw new IllegalArgumentException("Ya existe una moneda base en el sistema");
        }
        
        Currency currency = new Currency();
        currency.setCode(currencyRequest.getCode());
        currency.setName(currencyRequest.getName());
        currency.setSymbol(currencyRequest.getSymbol());
        currency.setExchangeRate(currencyRequest.getExchangeRate());
        currency.setBaseCurrency(currencyRequest.isBaseCurrency());
        
        Currency savedCurrency = currencyRepository.save(currency);
        logger.info("Moneda creada: {}", savedCurrency.getCode());
        
        return convertToCurrencyResponse(savedCurrency);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"currencies", "currency"}, allEntries = true)
    public CurrencyResponse updateCurrency(Long id, CurrencyRequest currencyRequest) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Moneda no encontrada con ID: " + id));
        
        // Verificar si el código ya está en uso por otra moneda
        if (!currency.getCode().equals(currencyRequest.getCode()) && 
                currencyRepository.existsByCode(currencyRequest.getCode())) {
            throw new IllegalArgumentException("Ya existe una moneda con el código: " + currencyRequest.getCode());
        }
        
        // Si se está cambiando a moneda base, verificar que no haya otra moneda base
        if (currencyRequest.isBaseCurrency() && !currency.isBaseCurrency() && 
                currencyRepository.countBaseCurrencies() > 0) {
            throw new IllegalArgumentException("Ya existe una moneda base en el sistema");
        }
        
        currency.setCode(currencyRequest.getCode());
        currency.setName(currencyRequest.getName());
        currency.setSymbol(currencyRequest.getSymbol());
        currency.setExchangeRate(currencyRequest.getExchangeRate());
        currency.setBaseCurrency(currencyRequest.isBaseCurrency());
        
        Currency updatedCurrency = currencyRepository.save(currency);
        logger.info("Moneda actualizada: {}", updatedCurrency.getCode());
        
        return convertToCurrencyResponse(updatedCurrency);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"currencies", "currency"}, allEntries = true)
    public void deleteCurrency(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Moneda no encontrada con ID: " + id));
        
        // No permitir eliminar la moneda base
        if (currency.isBaseCurrency()) {
            throw new IllegalArgumentException("No se puede eliminar la moneda base del sistema");
        }
        
        currencyRepository.delete(currency);
        logger.info("Moneda eliminada: {}", currency.getCode());
    }
    
    @Override
    @Cacheable("baseCurrency")
    public Currency getBaseCurrency() {
        return currencyRepository.findByBaseCurrencyTrue()
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una moneda base en el sistema"));
    }
    
    @Override
    public BigDecimal convertCurrency(BigDecimal amount, String fromCurrencyCode, String toCurrencyCode) {
        // Si las monedas son iguales, no hay conversión
        if (fromCurrencyCode.equals(toCurrencyCode)) {
            return amount;
        }
        
        Currency fromCurrency = currencyRepository.findByCode(fromCurrencyCode)
                .orElseThrow(() -> new EntityNotFoundException("Moneda origen no encontrada: " + fromCurrencyCode));
        
        Currency toCurrency = currencyRepository.findByCode(toCurrencyCode)
                .orElseThrow(() -> new EntityNotFoundException("Moneda destino no encontrada: " + toCurrencyCode));
        
        // Convertir a la moneda base primero (si no es la moneda base)
        BigDecimal amountInBaseCurrency;
        if (fromCurrency.isBaseCurrency()) {
            amountInBaseCurrency = amount;
        } else {
            amountInBaseCurrency = amount.divide(fromCurrency.getExchangeRate(), 6, RoundingMode.HALF_UP);
        }
        
        // Luego convertir de la moneda base a la moneda destino
        BigDecimal result;
        if (toCurrency.isBaseCurrency()) {
            result = amountInBaseCurrency;
        } else {
            result = amountInBaseCurrency.multiply(toCurrency.getExchangeRate());
        }
        
        // Redondear a 2 decimales
        return result.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public CurrencyResponse convertToCurrencyResponse(Currency currency) {
        CurrencyResponse response = new CurrencyResponse();
        response.setId(currency.getId());
        response.setCode(currency.getCode());
        response.setName(currency.getName());
        response.setSymbol(currency.getSymbol());
        response.setExchangeRate(currency.getExchangeRate());
        response.setBaseCurrency(currency.isBaseCurrency());
        response.setCreatedAt(currency.getCreatedAt());
        response.setUpdatedAt(currency.getUpdatedAt());
        return response;
    }
}
