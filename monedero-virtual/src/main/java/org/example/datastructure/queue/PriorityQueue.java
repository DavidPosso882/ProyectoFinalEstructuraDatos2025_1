package org.example.datastructure.queue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Implementación de una cola de prioridad genérica basada en un montículo binario (heap)

public class PriorityQueue<T> {
    
    private List<PriorityNode<T>> heap;
    private Comparator<T> comparator;
    public PriorityQueue(Comparator<T> comparator) {
        this.heap = new ArrayList<>();
        this.comparator = comparator;
    }
    
    @SuppressWarnings("unchecked")
    public PriorityQueue() {
        this.heap = new ArrayList<>();
        this.comparator = (a, b) -> {
            if (a instanceof Comparable && b instanceof Comparable) {
                return ((Comparable<T>) a).compareTo(b);
            }
            throw new IllegalArgumentException("Los elementos deben ser comparables o se debe proporcionar un comparador");
        };
    }
    
    // Inserta un elemento con una prioridad específica

    public void enqueue(T element, int priority) {
        PriorityNode<T> node = new PriorityNode<>(element, priority);
        heap.add(node);
        siftUp(heap.size() - 1);
    }
    
    // Inserta un elemento usando su orden natural como prioridad

    public void enqueue(T element) {
        // Usar el comparador para determinar la prioridad relativa
        int priority = 0;
        if (!heap.isEmpty()) {
            // Comparar con el elemento de mayor prioridad actual
            T highestPriorityElement = heap.get(0).getElement();
            priority = comparator.compare(element, highestPriorityElement);
            // Invertir para que menor valor = mayor prioridad
            priority = -priority;
        }
        enqueue(element, priority);
    }
    
    // Extrae el elemento de mayor prioridad

    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        
        T result = heap.get(0).getElement();
        
        // Mover el último elemento a la raíz y eliminar el último
        PriorityNode<T> lastNode = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, lastNode);
            siftDown(0);
        }
        
        return result;
    }
    
    // Consulta el elemento de mayor prioridad sin extraerlo

    public T peek() {
        return isEmpty() ? null : heap.get(0).getElement();
    }
    
    // Verifica si la cola está vacía

    public boolean isEmpty() {
        return heap.isEmpty();
    }
    
    // Obtiene el tamaño de la cola

    public int size() {
        return heap.size();
    }
    
    // Limpia la cola, eliminando todos los elementos
    public void clear() {
        heap.clear();
    }
    
    // Obtiene todos los elementos de la cola sin modificarla

    public List<T> toList() {
        List<T> result = new ArrayList<>(heap.size());
        for (PriorityNode<T> node : heap) {
            result.add(node.getElement());
        }
        return result;
    }
    
    // Reorganiza el heap hacia arriba desde la posición dada
 
    private void siftUp(int index) {
        int parentIndex = getParentIndex(index);
        
        while (index > 0 && heap.get(index).getPriority() > heap.get(parentIndex).getPriority()) {
            // Intercambiar con el padre
            swap(index, parentIndex);
            index = parentIndex;
            parentIndex = getParentIndex(index);
        }
    }
    
    // Reorganiza el heap hacia abajo desde la posición dada

    private void siftDown(int index) {
        int largestIndex = index;
        int leftChildIndex = getLeftChildIndex(index);
        int rightChildIndex = getRightChildIndex(index);
        
        // Comparar con hijo izquierdo
        if (leftChildIndex < heap.size() && 
            heap.get(leftChildIndex).getPriority() > heap.get(largestIndex).getPriority()) {
            largestIndex = leftChildIndex;
        }
        
        // Comparar con hijo derecho
        if (rightChildIndex < heap.size() && 
            heap.get(rightChildIndex).getPriority() > heap.get(largestIndex).getPriority()) {
            largestIndex = rightChildIndex;
        }
        
        // Si el mayor no es el actual, intercambiar y seguir reorganizando
        if (largestIndex != index) {
            swap(index, largestIndex);
            siftDown(largestIndex);
        }
    }
    
    // Intercambia dos elementos en el heap
    private void swap(int i, int j) {
        PriorityNode<T> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
    
    // Obtiene el índice del padre de un nodo
    private int getParentIndex(int index) {
        return (index - 1) / 2;
    }
    
    // Obtiene el índice del hijo izquierdo de un nodo

    private int getLeftChildIndex(int index) {
        return 2 * index + 1;
    }
    
    // Obtiene el índice del hijo derecho de un nodo
    private int getRightChildIndex(int index) {
        return 2 * index + 2;
    }
}
