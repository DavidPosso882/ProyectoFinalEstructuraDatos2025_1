package org.example.service;

import org.example.dto.request.CurrencyRequest;
import org.example.dto.response.CurrencyResponse;
import org.example.model.Currency;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para operaciones relacionadas con monedas.
 */
public interface CurrencyService {
    
    /**
     * Obtiene todas las monedas disponibles.
     * @return Lista de monedas
     */
    List<CurrencyResponse> getAllCurrencies();
    
    /**
     * Obtiene una moneda por su ID.
     * @param id ID de la moneda
     * @return Moneda encontrada
     */
    CurrencyResponse getCurrencyById(Long id);
    
    /**
     * Obtiene una moneda por su código.
     * @param code Código de la moneda
     * @return Moneda encontrada
     */
    CurrencyResponse getCurrencyByCode(String code);
    
    /**
     * Crea una nueva moneda.
     * @param currencyRequest Datos de la moneda
     * @return Moneda creada
     */
    CurrencyResponse createCurrency(CurrencyRequest currencyRequest);
    
    /**
     * Actualiza una moneda existente.
     * @param id ID de la moneda
     * @param currencyRequest Nuevos datos de la moneda
     * @return Moneda actualizada
     */
    CurrencyResponse updateCurrency(Long id, CurrencyRequest currencyRequest);
    
    /**
     * Elimina una moneda.
     * @param id ID de la moneda
     */
    void deleteCurrency(Long id);
    
    /**
     * Obtiene la moneda base del sistema.
     * @return Moneda base
     */
    Currency getBaseCurrency();
    
    /**
     * Convierte un monto de una moneda a otra.
     * @param amount Monto a convertir
     * @param fromCurrencyCode Código de la moneda origen
     * @param toCurrencyCode Código de la moneda destino
     * @return Monto convertido
     */
    BigDecimal convertCurrency(BigDecimal amount, String fromCurrencyCode, String toCurrencyCode);
    
    /**
     * Convierte un objeto Currency a CurrencyResponse.
     * @param currency Objeto Currency
     * @return Objeto CurrencyResponse
     */
    CurrencyResponse convertToCurrencyResponse(Currency currency);
}
