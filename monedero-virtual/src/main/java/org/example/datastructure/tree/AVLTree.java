package org.example.datastructure.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Implementación de un Árbol AVL (Árbol Binario de Búsqueda Balanceado)

public class AVLTree<T extends Comparable<T>> {
    
    private AVLNode<T> root;
    private int size;
    
    public AVLTree() {
        this.root = null;
        this.size = 0;
    }
    
    // Inserta un nuevo valor en el árbol

    public void insert(T value) {
        if (value == null) {
            throw new IllegalArgumentException("No se puede insertar un valor nulo");
        }
        
        root = insertRecursive(root, value);
    }
    
    private AVLNode<T> insertRecursive(AVLNode<T> node, T value) {
        // Inserción normal en BST
        if (node == null) {
            size++;
            return new AVLNode<>(value);
        }
        
        int compareResult = value.compareTo(node.getValue());
        
        if (compareResult < 0) {
            node.setLeft(insertRecursive(node.getLeft(), value));
        } else if (compareResult > 0) {
            node.setRight(insertRecursive(node.getRight(), value));
        } else {
            // Valor duplicado, actualizamos el nodo
            node.setValue(value);
            return node;
        }
        
        // Actualizar altura
        updateHeight(node);
        
        // Rebalancear el árbol
        return rebalance(node);
    }
    
    // Busca un valor en el árbol

    public boolean contains(T value) {
        return findNode(root, value) != null;
    }
    
    // Obtiene un valor del árbol

    public T find(T value) {
        AVLNode<T> node = findNode(root, value);
        return node != null ? node.getValue() : null;
    }
    
    private AVLNode<T> findNode(AVLNode<T> current, T value) {
        if (current == null || value == null) {
            return null;
        }
        
        int compareResult = value.compareTo(current.getValue());
        
        if (compareResult == 0) {
            return current;
        } else if (compareResult < 0) {
            return findNode(current.getLeft(), value);
        } else {
            return findNode(current.getRight(), value);
        }
    }
    
    // Elimina un valor del árbol

    public boolean remove(T value) {
        if (root == null || value == null) {
            return false;
        }
        
        int initialSize = size;
        root = removeRecursive(root, value);
        return size < initialSize;
    }
    
    private AVLNode<T> removeRecursive(AVLNode<T> node, T value) {
        if (node == null) {
            return null;
        }
        
        int compareResult = value.compareTo(node.getValue());
        
        if (compareResult < 0) {
            node.setLeft(removeRecursive(node.getLeft(), value));
        } else if (compareResult > 0) {
            node.setRight(removeRecursive(node.getRight(), value));
        } else {
            // Caso 1: Nodo hoja (sin hijos)
            if (node.getLeft() == null && node.getRight() == null) {
                size--;
                return null;
            }
            
            // Caso 2: Nodo con un solo hijo
            if (node.getLeft() == null) {
                size--;
                return node.getRight();
            }
            
            if (node.getRight() == null) {
                size--;
                return node.getLeft();
            }
            
            // Caso 3: Nodo con dos hijos
            // Encontrar el sucesor (el menor valor en el subárbol derecho)
            T successorValue = findMin(node.getRight());
            node.setValue(successorValue);
            node.setRight(removeRecursive(node.getRight(), successorValue));
        }
        
        if (node == null) {
            return null;
        }
        
        
        // Actualizar altura
        updateHeight(node);
        
        // Rebalancear el árbol
        return rebalance(node);
    }
    
    // Encuentra el valor mínimo en un subárbol

    private T findMin(AVLNode<T> node) {
        if (node.getLeft() == null) {
            return node.getValue();
        }
        return findMin(node.getLeft());
    }
    
    // Actualiza la altura de un nodo
    
    private void updateHeight(AVLNode<T> node) {
        int leftHeight = (node.getLeft() != null) ? node.getLeft().getHeight() : 0;
        int rightHeight = (node.getRight() != null) ? node.getRight().getHeight() : 0;
        node.setHeight(Math.max(leftHeight, rightHeight) + 1);
    }
    
    // Calcula el factor de balance de un nodo
    private int getBalanceFactor(AVLNode<T> node) {
        if (node == null) {
            return 0;
        }
        
        int leftHeight = (node.getLeft() != null) ? node.getLeft().getHeight() : 0;
        int rightHeight = (node.getRight() != null) ? node.getRight().getHeight() : 0;
        
        return leftHeight - rightHeight;
    }
    
    // Rebalancea un nodo si es necesario

    private AVLNode<T> rebalance(AVLNode<T> node) {
        int balanceFactor = getBalanceFactor(node);
        
        // Caso de desbalance izquierdo
        if (balanceFactor > 1) {
            // Caso izquierda-derecha (LR)
            if (getBalanceFactor(node.getLeft()) < 0) {
                node.setLeft(rotateLeft(node.getLeft()));
            }
            // Caso izquierda-izquierda (LL)
            return rotateRight(node);
        }
        
        // Caso de desbalance derecho
        if (balanceFactor < -1) {
            // Caso derecha-izquierda (RL)
            if (getBalanceFactor(node.getRight()) > 0) {
                node.setRight(rotateRight(node.getRight()));
            }
            // Caso derecha-derecha (RR)
            return rotateLeft(node);
        }
        
        return node;
    }
    
    // Rotación a la derecha

    private AVLNode<T> rotateRight(AVLNode<T> y) {
        AVLNode<T> x = y.getLeft();
        AVLNode<T> T2 = x.getRight();
        
        // Realizar rotación
        x.setRight(y);
        y.setLeft(T2);
        
        // Actualizar alturas
        updateHeight(y);
        updateHeight(x);
        
        return x;
    }
    
    /**
     * Rotación a la izquierda
     */
    private AVLNode<T> rotateLeft(AVLNode<T> x) {
        AVLNode<T> y = x.getRight();
        AVLNode<T> T2 = y.getLeft();
        
        // Realizar rotación
        y.setLeft(x);
        x.setRight(T2);
        
        // Actualizar alturas
        updateHeight(x);
        updateHeight(y);
        
        return y;
    }
    
    /**
     * Recorre el árbol en orden (inorder traversal)
     * @param consumer Función a aplicar a cada nodo
     */
    public void inOrderTraversal(Consumer<T> consumer) {
        inOrderTraversal(root, consumer);
    }
    
    private void inOrderTraversal(AVLNode<T> node, Consumer<T> consumer) {
        if (node != null) {
            inOrderTraversal(node.getLeft(), consumer);
            consumer.accept(node.getValue());
            inOrderTraversal(node.getRight(), consumer);
        }
    }
    
    // Obtiene todos los valores del árbol en orden ascendente
    public List<T> toSortedList() {
        List<T> result = new ArrayList<>();
        inOrderTraversal(value -> result.add(value));
        return result;
    }
    
    // Obtiene el tamaño del árbol

    public int size() {
        return size;
    }
    
    // Verifica si el árbol está vacío
    public boolean isEmpty() {
        return size == 0;
    }
    
    // Limpia el árbol, eliminando todos los nodos
    public void clear() {
        root = null;
        size = 0;
    }
}
