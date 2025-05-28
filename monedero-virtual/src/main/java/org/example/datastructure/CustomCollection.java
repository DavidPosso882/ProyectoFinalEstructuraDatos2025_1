package org.example.datastructure;

/**
 * Interfaz base para todas las colecciones personalizadas.
 * @param <T> Tipo de elementos en la colección
 */
public interface CustomCollection<T> {
    
    /**
     * Añade un elemento a la colección.
     * @param element Elemento a añadir
     * @return true si se añadió correctamente, false en caso contrario
     */
    boolean add(T element);
    
    /**
     * Elimina un elemento de la colección.
     * @param element Elemento a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean remove(T element);
    
    /**
     * Verifica si la colección contiene un elemento.
     * @param element Elemento a verificar
     * @return true si la colección contiene el elemento, false en caso contrario
     */
    boolean contains(T element);
    
    /**
     * Devuelve el número de elementos en la colección.
     * @return Número de elementos
     */
    int size();
    
    /**
     * Verifica si la colección está vacía.
     * @return true si la colección está vacía, false en caso contrario
     */
    boolean isEmpty();
    
    /**
     * Elimina todos los elementos de la colección.
     */
    void clear();
    
    /**
     * Convierte la colección a un array.
     * @return Array con los elementos de la colección
     */
    Object[] toArray();
}
