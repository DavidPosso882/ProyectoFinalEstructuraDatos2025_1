package org.example.datastructure.graph;

import lombok.Data;

// Representa una arista completa con origen, destino y peso

@Data
public class EdgeTriple<T> {
    private T source;
    private T destination;
    private double weight;
    
    public EdgeTriple(T source, T destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }
}
