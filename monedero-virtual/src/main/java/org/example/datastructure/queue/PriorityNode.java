package org.example.datastructure.queue;

import lombok.Data;

@Data
public class PriorityNode<T> {
    private T element;
    private int priority;
    
    public PriorityNode(T element, int priority) {
        this.element = element;
        this.priority = priority;
    }
}
