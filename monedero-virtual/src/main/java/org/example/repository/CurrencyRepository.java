package org.example.repository;

import org.example.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos relacionadas con monedas.
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    
    /**
     * Busca una moneda por su código.
     * @param code Código de la moneda (ej. USD, EUR)
     * @return Moneda encontrada
     */
    Optional<Currency> findByCode(String code);
    
    /**
     * Busca la moneda base del sistema.
     * @return Moneda base
     */
    Optional<Currency> findByBaseCurrencyTrue();
    
    /**
     * Verifica si existe una moneda con el código especificado.
     * @param code Código de la moneda
     * @return true si existe, false en caso contrario
     */
    boolean existsByCode(String code);
    
    /**
     * Cuenta cuántas monedas base hay en el sistema.
     * @return Número de monedas base
     */
    @Query("SELECT COUNT(c) FROM Currency c WHERE c.baseCurrency = true")
    long countBaseCurrencies();
}
