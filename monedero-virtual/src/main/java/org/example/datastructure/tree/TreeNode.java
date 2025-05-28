package org.example.datastructure.tree;

import lombok.Data;

@Data
public class TreeNode<T> {
    private T value;
    private TreeNode<T> left;
    private TreeNode<T> right;
    
    public TreeNode(T value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }
}
