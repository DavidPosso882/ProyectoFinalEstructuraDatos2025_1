package org.example.datastructure.list;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CircularLinkedList<T> implements Iterable<T> {
    
    private Node<T> head;
    private Node<T> tail;
    private int size;
    
    public CircularLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }
    
    // Añade un elemento al final de la lista

    public void add(T element) {
        Node<T> newNode = new Node<>(element);
        
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
            // Hacer circular
            newNode.setNext(newNode);
        } else {
            // Insertar después del último
            newNode.setNext(head);
            tail.setNext(newNode);
            tail = newNode;
        }
        
        size++;
    }
    
    // Añade un elemento en una posición específica

    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        
        // Añadir al finañ
        if (index == size) {
            add(element);
            return;
        }
        
        Node<T> newNode = new Node<>(element);
        
        // lista vacía o añadir al principio
        if (isEmpty() || index == 0) {
            if (isEmpty()) {
                newNode.setNext(newNode); // Auto-referencia si está vacía
                tail = newNode;
            } else {
                newNode.setNext(head);
                tail.setNext(newNode);
            }
            head = newNode;
        } else {
            // Encontrar el nodo anterior a la posición de inserción
            Node<T> current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.getNext();
            }
            
            // Insertar después del nodo actual
            newNode.setNext(current.getNext());
            current.setNext(newNode);
            
            // Actualizar tail si es necesario
            if (current == tail) {
                tail = newNode;
            }
        }
        
        size++;
    }
    
    // Elimina un elemento en una posición específica

    public T remove(int index) {
        if (isEmpty() || index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        
        T removedElement;
        
        // Caso especial: eliminar el único elemento
        if (size == 1) {
            removedElement = head.getElement();
            head = null;
            tail = null;
        }
        // Caso especial: eliminar el primero
        else if (index == 0) {
            removedElement = head.getElement();
            head = head.getNext();
            tail.setNext(head);
        }
        // Eliminar en otra posición
        else {
            // Encontrar el nodo anterior al que se va a eliminar
            Node<T> current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.getNext();
            }
            
            removedElement = current.getNext().getElement();
            
            // Actualizar tail si es necesario
            if (current.getNext() == tail) {
                tail = current;
            }
            
            current.setNext(current.getNext().getNext());
        }
        
        size--;
        return removedElement;
    }
    
    // Elimina la primera ocurrencia de un elemento

    public boolean remove(T element) {
        if (isEmpty()) {
            return false;
        }
        
        // Caso especial: eliminar el primer elemento
        if (head.getElement().equals(element)) {
            if (size == 1) {
                head = null;
                tail = null;
            } else {
                head = head.getNext();
                tail.setNext(head);
            }
            size--;
            return true;
        }
        
        // Buscar el elemento
        Node<T> current = head;
        while (current.getNext() != head) {
            if (current.getNext().getElement().equals(element)) {
                // Actualizar tail si es necesario
                if (current.getNext() == tail) {
                    tail = current;
                }
                
                current.setNext(current.getNext().getNext());
                size--;
                return true;
            }
            current = current.getNext();
        }
        
        return false;
    }
    
    // Obtiene el elemento en una posición específica

    public T get(int index) {
        if (isEmpty() || index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        
        return current.getElement();
    }
    
    // Actualiza el elemento en una posición específica

    public T set(int index, T element) {
        if (isEmpty() || index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        
        T oldElement = current.getElement();
        current.setElement(element);
        
        return oldElement;
    }
    
    // Busca la posición de un elemento

    public int indexOf(T element) {
        if (isEmpty()) {
            return -1;
        }
        
        Node<T> current = head;
        for (int i = 0; i < size; i++) {
            if (current.getElement().equals(element)) {
                return i;
            }
            current = current.getNext();
        }
        
        return -1;
    }
    
    // Verifica si la lista contiene un elemento

    public boolean contains(T element) {
        return indexOf(element) != -1;
    }
    
    // Obtiene el tamaño de la lista

    public int size() {
        return size;
    }
    
    // Verifica si la lista está vacía

    public boolean isEmpty() {
        return size == 0;
    }
    
    // Limpia la lista, eliminando todos los elementos
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
    
    // Rota la lista un número específico de posiciones

    public void rotate(int positions) {
        if (isEmpty() || positions % size == 0) {
            return;
        }
        
        // Normalizar posiciones
        positions = positions % size;
        if (positions < 0) {
            positions += size; // Convertir rotación negativa a equivalente positiva
        }
        
        // Rotar la lista
        for (int i = 0; i < positions; i++) {
            head = head.getNext();
            tail = tail.getNext();
        }
    }
    
    @Override
    public Iterator<T> iterator() {
        return new CircularIterator();
    }
    
    // Iterador para la lista circular
    
    private class CircularIterator implements Iterator<T> {
        private Node<T> current;
        private int count;
        
        public CircularIterator() {
            this.current = head;
            this.count = 0;
        }
        
        @Override
        public boolean hasNext() {
            return !isEmpty() && count < size;
        }
        
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            T element = current.getElement();
            current = current.getNext();
            count++;
            
            return element;
        }
    }
}
