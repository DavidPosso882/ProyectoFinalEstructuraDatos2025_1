package org.example.datastructure.tree;

import lombok.Data;

@Data
public class AVLNode<T> {
    private T value;
    private AVLNode<T> left;
    private AVLNode<T> right;
    private int height;
    
    public AVLNode(T value) {
        this.value = value;
        this.left = null;
        this.right = null;
        this.height = 1; // Altura inicial de un nodo hoja
    }
}
