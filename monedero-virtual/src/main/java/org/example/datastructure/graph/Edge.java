package org.example.datastructure.graph;

import lombok.Data;

// Representa una arista dirigida con peso en un grafo

@Data
public class Edge<T> {
    private T destination;
    private double weight;
    
    public Edge(T destination, double weight) {
        this.destination = destination;
        this.weight = weight;
    }
}
