package org.example.datastructure.list;

import lombok.Data;

@Data
public class Node<T> {
    private T element;
    private Node<T> next;
    
    public Node(T element) {
        this.element = element;
        this.next = null;
    }
}
