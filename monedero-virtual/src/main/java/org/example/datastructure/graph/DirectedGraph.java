package org.example.datastructure.graph;

import java.util.*;

// Implementación de un grafo digido con pesos
public class DirectedGraph<T> {
    
    // Mapa de adyacencia: vértice -> lista de aristas salientes
    private Map<T, List<Edge<T>>> adjacencyMap;
    
    public DirectedGraph() {
        this.adjacencyMap = new HashMap<>();
    }
    
    // Añade un vertice al grago
    public boolean addVertex(T vertex) {
        if (vertex == null || adjacencyMap.containsKey(vertex)) {
            return false;
        }
        
        adjacencyMap.put(vertex, new ArrayList<>());
        return true;
    }
    

    // Añade una arista dirigida con peso entre dos vértices

    public boolean addEdge(T source, T destination, double weight) {
        if (source == null || destination == null) {
            return false;
        }
        
        // Asegurarse de que ambos vértices existan
        addVertex(source);
        addVertex(destination);
        
        // Verificar si la arista ya existe
        List<Edge<T>> edges = adjacencyMap.get(source);
        for (Edge<T> edge : edges) {
            if (edge.getDestination().equals(destination)) {
                // Actualizar el peso si la arista ya existe
                edge.setWeight(weight);
                return false;
            }
        }
        
        // Añadir nueva arista
        edges.add(new Edge<>(destination, weight));
        return true;
    }
    
    // Elimina un vértice y todas sus aristas asociadas

    public boolean removeVertex(T vertex) {
        if (vertex == null || !adjacencyMap.containsKey(vertex)) {
            return false;
        }
        
        // Eliminar el vértice del mapa de adyacencia
        adjacencyMap.remove(vertex);
        
        // Eliminar todas las aristas que apuntan a este vértice
        for (List<Edge<T>> edges : adjacencyMap.values()) {
            edges.removeIf(edge -> edge.getDestination().equals(vertex));
        }
        
        return true;
    }
    

    // Elimina una arista entre dos vértices

    public boolean removeEdge(T source, T destination) {
        if (source == null || destination == null || !adjacencyMap.containsKey(source)) {
            return false;
        }
        
        List<Edge<T>> edges = adjacencyMap.get(source);
        return edges.removeIf(edge -> edge.getDestination().equals(destination));
    }
    
    // Obtiene todos los vértices del grafo

    public Set<T> getVertices() {
        return new HashSet<>(adjacencyMap.keySet());
    }
    
    
    // Obtiene todas las aristas salientes de un vértice

    public List<Edge<T>> getEdges(T vertex) {
        if (vertex == null || !adjacencyMap.containsKey(vertex)) {
            return null;
        }
        
        return new ArrayList<>(adjacencyMap.get(vertex));
    }
    
    //Obtiene todas las aristas del grafo

    public List<EdgeTriple<T>> getAllEdges() {
        List<EdgeTriple<T>> allEdges = new ArrayList<>();
        
        for (Map.Entry<T, List<Edge<T>>> entry : adjacencyMap.entrySet()) {
            T source = entry.getKey();
            for (Edge<T> edge : entry.getValue()) {
                allEdges.add(new EdgeTriple<>(source, edge.getDestination(), edge.getWeight()));
            }
        }
        
        return allEdges;
    }
    
    // Verifica si existe una arista entre dos vértices

    public boolean hasEdge(T source, T destination) {
        if (source == null || destination == null || !adjacencyMap.containsKey(source)) {
            return false;
        }
        
        for (Edge<T> edge : adjacencyMap.get(source)) {
            if (edge.getDestination().equals(destination)) {
                return true;
            }
        }
        
        return false;
    }
    
    // Obtiene el peso de una arista entre dos vértices

    public double getEdgeWeight(T source, T destination) {
        if (source == null || destination == null || !adjacencyMap.containsKey(source)) {
            return -1;
        }
        
        for (Edge<T> edge : adjacencyMap.get(source)) {
            if (edge.getDestination().equals(destination)) {
                return edge.getWeight();
            }
        }
        
        return -1;
    }
    
    // Obtiene los vértices adyacentes a un vértice dado

    public Set<T> getAdjacentVertices(T vertex) {
        if (vertex == null || !adjacencyMap.containsKey(vertex)) {
            return null;
        }
        
        Set<T> adjacentVertices = new HashSet<>();
        for (Edge<T> edge : adjacencyMap.get(vertex)) {
            adjacentVertices.add(edge.getDestination());
        }
        
        return adjacentVertices;
    }
    
    // Obtiene el número de vértices en el grafo

    public int getVertexCount() {
        return adjacencyMap.size();
    }
    
    // Obtiene el número de aristas en el grafo

    public int getEdgeCount() {
        int count = 0;
        for (List<Edge<T>> edges : adjacencyMap.values()) {
            count += edges.size();
        }
        return count;
    }
    
    // Verifica si el grafo está vacío

    public boolean isEmpty() {
        return adjacencyMap.isEmpty();
    }
    
    // Limpia el grafo, eliminando todos los vértices y aristas
    
    public void clear() {
        adjacencyMap.clear();
    }
    
    // Realiza un recorrido en anchura (BFS) desde un vértice origen

    public List<T> breadthFirstTraversal(T start) {
        if (start == null || !adjacencyMap.containsKey(start)) {
            return new ArrayList<>();
        }
        
        List<T> result = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        Queue<T> queue = new LinkedList<>();
        
        queue.add(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            T current = queue.poll();
            result.add(current);
            
            for (Edge<T> edge : adjacencyMap.get(current)) {
                T neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        return result;
    }
    
    // Realiza un recorrido en profundidad (DFS) desde un vértice origen

    public List<T> depthFirstTraversal(T start) {
        if (start == null || !adjacencyMap.containsKey(start)) {
            return new ArrayList<>();
        }
        
        List<T> result = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        
        depthFirstTraversalHelper(start, visited, result);
        
        return result;
    }
    
    private void depthFirstTraversalHelper(T current, Set<T> visited, List<T> result) {
        visited.add(current);
        result.add(current);
        
        for (Edge<T> edge : adjacencyMap.get(current)) {
            T neighbor = edge.getDestination();
            if (!visited.contains(neighbor)) {
                depthFirstTraversalHelper(neighbor, visited, result);
            }
        }
    }
}
