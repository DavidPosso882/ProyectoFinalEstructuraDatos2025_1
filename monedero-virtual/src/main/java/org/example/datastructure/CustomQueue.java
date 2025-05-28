package org.example.datastructure;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CustomQueue<T> implements CustomCollection<T>, Iterable<T> {
    
    private class Node {
        T data;
        Node next;
        
        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
    
    private Node front;
    private Node rear;
    private int size;
    
    public CustomQueue() {
        front = null;
        rear = null;
        size = 0;
    }
    
    // Añade un elemento al final de la cola.

    @Override
    public boolean add(T element) {
        return enqueue(element);
    }
    
    // Añade un elemento al final de la cola.

    public boolean enqueue(T element) {
        if (element == null) {
            return false;
        }
        
        Node newNode = new Node(element);
        
        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        
        size++;
        return true;
    }
    
    // Elimina y devuelve el elemento al frente de la cola.

    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        
        T data = front.data;
        front = front.next;
        
        if (front == null) {
            rear = null;
        }
        
        size--;
        return data;
    }
    
    // Devuelve el elemento al frente de la cola sin eliminarlo.

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        
        return front.data;
    }
    
    @Override
    public boolean remove(T element) {
        if (element == null || isEmpty()) {
            return false;
        }
        
        if (front.data.equals(element)) {
            front = front.next;
            
            if (front == null) {
                rear = null;
            }
            
            size--;
            return true;
        }
        
        Node current = front;
        
        while (current.next != null && !current.next.data.equals(element)) {
            current = current.next;
        }
        
        if (current.next != null) {
            if (current.next == rear) {
                rear = current;
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
        
        Node current = front;
        
        while (current != null) {
            if (element.equals(current.data)) {
                return true;
            }
            current = current.next;
        }
        
        return false;
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
        front = null;
        rear = null;
        size = 0;
    }
    
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        Node current = front;
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
            private Node current = front;
            
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
        Node current = front;
        
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
