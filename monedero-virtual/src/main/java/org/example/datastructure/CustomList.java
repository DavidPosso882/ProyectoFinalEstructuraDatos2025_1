package org.example.datastructure;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementación personalizada de una lista enlazada genérica.
 * @param <T> Tipo de elementos en la lista
 */
public class CustomList<T> implements CustomCollection<T>, Iterable<T> {
    
    /**
     * Clase interna para representar un nodo en la lista enlazada.
     */
    private class Node {
        T data;
        Node next;
        
        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
    
    private Node head;
    private Node tail;
    private int size;
    
    /**
     * Constructor que crea una lista vacía.
     */
    public CustomList() {
        head = null;
        tail = null;
        size = 0;
    }
    
    @Override
    public boolean add(T element) {
        if (element == null) {
            return false;
        }
        
        Node newNode = new Node(element);
        
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        
        size++;
        return true;
    }
    
    /**
     * Añade un elemento al principio de la lista.
     * @param element Elemento a añadir
     * @return true si se añadió correctamente, false en caso contrario
     */
    public boolean addFirst(T element) {
        if (element == null) {
            return false;
        }
        
        Node newNode = new Node(element);
        
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head = newNode;
        }
        
        size++;
        return true;
    }
    
    /**
     * Añade un elemento en una posición específica.
     * @param index Posición donde añadir el elemento
     * @param element Elemento a añadir
     * @return true si se añadió correctamente, false en caso contrario
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public boolean add(int index, T element) {
        if (element == null) {
            return false;
        }
        
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        
        if (index == 0) {
            return addFirst(element);
        }
        
        if (index == size) {
            return add(element);
        }
        
        Node current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }
        
        Node newNode = new Node(element);
        newNode.next = current.next;
        current.next = newNode;
        
        size++;
        return true;
    }
    
    /**
     * Obtiene el elemento en una posición específica.
     * @param index Posición del elemento
     * @return Elemento en la posición especificada
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        
        return current.data;
    }
    
    /**
     * Establece un elemento en una posición específica.
     * @param index Posición donde establecer el elemento
     * @param element Elemento a establecer
     * @return Elemento anterior en esa posición
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public T set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        
        if (element == null) {
            throw new NullPointerException("No se permiten elementos nulos");
        }
        
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        
        T oldValue = current.data;
        current.data = element;
        
        return oldValue;
    }
    
    /**
     * Elimina el elemento en una posición específica.
     * @param index Posición del elemento a eliminar
     * @return Elemento eliminado
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        
        T removedElement;
        
        if (index == 0) {
            removedElement = head.data;
            head = head.next;
            
            if (head == null) {
                tail = null;
            }
        } else {
            Node current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            
            removedElement = current.next.data;
            
            if (current.next == tail) {
                tail = current;
            }
            
            current.next = current.next.next;
        }
        
        size--;
        return removedElement;
    }
    
    @Override
    public boolean remove(T element) {
        if (element == null || isEmpty()) {
            return false;
        }
        
        if (head.data.equals(element)) {
            head = head.next;
            
            if (head == null) {
                tail = null;
            }
            
            size--;
            return true;
        }
        
        Node current = head;
        while (current.next != null && !current.next.data.equals(element)) {
            current = current.next;
        }
        
        if (current.next != null) {
            if (current.next == tail) {
                tail = current;
            }
            
            current.next = current.next.next;
            size--;
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean contains(T element) {
        if (element == null || isEmpty()) {
            return false;
        }
        
        Node current = head;
        while (current != null) {
            if (element.equals(current.data)) {
                return true;
            }
            current = current.next;
        }
        
        return false;
    }
    
    /**
     * Encuentra el índice de la primera ocurrencia de un elemento.
     * @param element Elemento a buscar
     * @return Índice del elemento, o -1 si no se encuentra
     */
    public int indexOf(T element) {
        if (element == null || isEmpty()) {
            return -1;
        }
        
        Node current = head;
        int index = 0;
        
        while (current != null) {
            if (element.equals(current.data)) {
                return index;
            }
            current = current.next;
            index++;
        }
        
        return -1;
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    @Override
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
    
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        Node current = head;
        int index = 0;
        
        while (current != null) {
            array[index++] = current.data;
            current = current.next;
        }
        
        return array;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node current = head;
            
            @Override
            public boolean hasNext() {
                return current != null;
            }
            
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }
    
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        Node current = head;
        
        while (current != null) {
            sb.append(current.data);
            
            if (current.next != null) {
                sb.append(", ");
            }
            
            current = current.next;
        }
        
        sb.append("]");
        return sb.toString();
    }
}
