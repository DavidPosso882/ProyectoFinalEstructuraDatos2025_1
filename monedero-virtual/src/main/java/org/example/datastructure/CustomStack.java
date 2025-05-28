package org.example.datastructure;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementación personalizada de una pila genérica.
 * @param <T> Tipo de elementos en la pila
 */
public class CustomStack<T> implements CustomCollection<T>, Iterable<T> {

    /**
     * Clase interna para representar un nodo en la pila.
     */
    private class Node {
        T data;
        Node next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node top;
    private int size;

    /**
     * Constructor que crea una pila vacía.
     */
    public CustomStack() {
        top = null;
        size = 0;
    }

    /**
     * Añade un elemento a la parte superior de la pila.
     * @param element Elemento a añadir
     * @return true si se añadió correctamente, false en caso contrario
     */
    @Override
    public boolean add(T element) {
        return push(element);
    }

    /**
     * Añade un elemento a la parte superior de la pila.
     * @param element Elemento a añadir
     * @return true si se añadió correctamente, false en caso contrario
     */
    public boolean push(T element) {
        if (element == null) {
            return false;
        }

        Node newNode = new Node(element);
        newNode.next = top;
        top = newNode;
        size++;

        return true;
    }

    /**
     * Elimina y devuelve el elemento en la parte superior de la pila.
     * @return Elemento en la parte superior de la pila
     * @throws EmptyStackException si la pila está vacía
     */
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }

        T data = top.data;
        top = top.next;
        size--;

        return data;
    }

    /**
     * Devuelve el elemento en la parte superior de la pila sin eliminarlo.
     * @return Elemento en la parte superior de la pila
     * @throws EmptyStackException si la pila está vacía
     */
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }

        return top.data;
    }

    @Override
    public boolean remove(T element) {
        if (element == null || isEmpty()) {
            return false;
        }

        if (top.data.equals(element)) {
            top = top.next;
            size--;
            return true;
        }

        Node current = top;
        Node prev = null;

        while (current != null && !current.data.equals(element)) {
            prev = current;
            current = current.next;
        }

        if (current != null) {
            prev.next = current.next;
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

        Node current = top;

        while (current != null) {
            if (element.equals(current.data)) {
                return true;
            }
            current = current.next;
        }

        return false;
    }

    /**
     * Busca un elemento en la pila y devuelve su posición (0 es el tope).
     * @param element Elemento a buscar
     * @return Posición del elemento, o -1 si no se encuentra
     */
    public int search(T element) {
        if (element == null || isEmpty()) {
            return -1;
        }

        Node current = top;
        int position = 0;

        while (current != null) {
            if (element.equals(current.data)) {
                return position;
            }
            current = current.next;
            position++;
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
        top = null;
        size = 0;
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        Node current = top;
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
            private Node current = top;

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
        Node current = top;

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

    /**
     * Elimina el último elemento de la pila (el que está en la base)
     * @return true si se eliminó correctamente, false si la pila está vacía
     */
    public boolean removeLast() {
        if (isEmpty()) {
            return false;
        }

        // Si solo hay un elemento, es el mismo que el tope
        if (size == 1) {
            top = null;
            size = 0;
            return true;
        }

        // Navegar hasta el penúltimo nodo
        Node current = top;
        while (current.next.next != null) {
            current = current.next;
        }

        // Eliminar el último nodo
        current.next = null;
        size--;
        return true;
    }
}
