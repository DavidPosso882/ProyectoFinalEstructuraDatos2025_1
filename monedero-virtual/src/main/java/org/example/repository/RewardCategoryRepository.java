package org.example.repository;

import org.example.model.RewardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardCategoryRepository extends JpaRepository<RewardCategory, Long> {
    
    /**
     * Busca una categoría por su nombre
     * @param name Nombre de la categoría
     * @return Categoría encontrada
     */
    Optional<RewardCategory> findByName(String name);
    
    /**
     * Verifica si existe una categoría con el nombre especificado
     * @param name Nombre de la categoría
     * @return true si existe, false en caso contrario
     */
    boolean existsByName(String name);
    
    /**
     * Busca todas las categorías activas
     * @return Lista de categorías activas
     */
    List<RewardCategory> findByActiveTrue();
    
    /**
     * Busca todas las categorías ordenadas por orden de visualización
     * @return Lista de categorías ordenadas
     */
    List<RewardCategory> findAllByOrderByDisplayOrderAsc();
    
    /**
     * Busca todas las categorías activas ordenadas por orden de visualización
     * @return Lista de categorías activas ordenadas
     */
    List<RewardCategory> findByActiveTrueOrderByDisplayOrderAsc();
}
