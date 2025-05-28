package org.example.datastructure.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementación genérica de un Árbol Binario de Búsqueda (BST)
 * @param <T> Tipo de datos que almacena el árbol, debe ser comparable
 */
public class BinarySearchTree<T extends Comparable<T>> {
    
    private TreeNode<T> root;
    private int size;
    
    public BinarySearchTree() {
        this.root = null;
        this.size = 0;
    }
    
    /**
     * Inserta un nuevo valor en el árbol
     * @param value Valor a insertar
     */
    public void insert(T value) {
        if (value == null) {
            throw new IllegalArgumentException("No se puede insertar un valor nulo");
        }
        
        if (root == null) {
            root = new TreeNode<>(value);
            size++;
            return;
        }
        
        insertRecursive(root, value);
    }
    
    private void insertRecursive(TreeNode<T> current, T value) {
        int compareResult = value.compareTo(current.getValue());
        
        if (compareResult < 0) {
            // El valor es menor, va a la izquierda
            if (current.getLeft() == null) {
                current.setLeft(new TreeNode<>(value));
                size++;
            } else {
                insertRecursive(current.getLeft(), value);
            }
        } else if (compareResult > 0) {
            // El valor es mayor, va a la derecha
            if (current.getRight() == null) {
                current.setRight(new TreeNode<>(value));
                size++;
            } else {
                insertRecursive(current.getRight(), value);
            }
        } else {
            // El valor ya existe, actualizamos el nodo
            current.setValue(value);
        }
    }
    
    /**
     * Busca un valor en el árbol
     * @param value Valor a buscar
     * @return true si el valor existe, false en caso contrario
     */
    public boolean contains(T value) {
        return findNode(root, value) != null;
    }
    
    /**
     * Obtiene un valor del árbol
     * @param value Valor a buscar
     * @return El valor encontrado o null si no existe
     */
    public T find(T value) {
        TreeNode<T> node = findNode(root, value);
        return node != null ? node.getValue() : null;
    }
    
    private TreeNode<T> findNode(TreeNode<T> current, T value) {
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
    
    /**
     * Elimina un valor del árbol
     * @param value Valor a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    public boolean remove(T value) {
        if (root == null || value == null) {
            return false;
        }
        
        int initialSize = size;
        root = removeRecursive(root, value);
        return size < initialSize;
    }
    
    private TreeNode<T> removeRecursive(TreeNode<T> current, T value) {
        if (current == null) {
            return null;
        }
        
        int compareResult = value.compareTo(current.getValue());
        
        if (compareResult < 0) {
            current.setLeft(removeRecursive(current.getLeft(), value));
        } else if (compareResult > 0) {
            current.setRight(removeRecursive(current.getRight(), value));
        } else {
            // Caso 1: Nodo hoja (sin hijos)
            if (current.getLeft() == null && current.getRight() == null) {
                size--;
                return null;
            }
            
            // Caso 2: Nodo con un solo hijo
            if (current.getLeft() == null) {
                size--;
                return current.getRight();
            }
            
            if (current.getRight() == null) {
                size--;
                return current.getLeft();
            }
            
            // Caso 3: Nodo con dos hijos
            // Encontrar el sucesor (el menor valor en el subárbol derecho)
            T successorValue = findMin(current.getRight());
            current.setValue(successorValue);
            current.setRight(removeRecursive(current.getRight(), successorValue));
        }
        
        return current;
    }
    
    /**
     * Encuentra el valor mínimo en un subárbol
     */
    private T findMin(TreeNode<T> node) {
        if (node.getLeft() == null) {
            return node.getValue();
        }
        return findMin(node.getLeft());
    }
    
    /**
     * Recorre el árbol en orden (inorder traversal)
     * @param consumer Función a aplicar a cada nodo
     */
    public void inOrderTraversal(Consumer<T> consumer) {
        inOrderTraversal(root, consumer);
    }
    
    private void inOrderTraversal(TreeNode<T> node, Consumer<T> consumer) {
        if (node != null) {
            inOrderTraversal(node.getLeft(), consumer);
            consumer.accept(node.getValue());
            inOrderTraversal(node.getRight(), consumer);
        }
    }
    
    /**
     * Obtiene todos los valores del árbol en orden ascendente
     * @return Lista de valores ordenados
     */
    public List<T> toSortedList() {
        List<T> result = new ArrayList<>();
        inOrderTraversal(value -> result.add(value));
        return result;
    }
    
    /**
     * Obtiene el tamaño del árbol
     * @return Número de nodos en el árbol
     */
    public int size() {
        return size;
    }
    
    /**
     * Verifica si el árbol está vacío
     * @return true si está vacío, false en caso contrario
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Limpia el árbol, eliminando todos los nodos
     */
    public void clear() {
        root = null;
        size = 0;
    }
}
