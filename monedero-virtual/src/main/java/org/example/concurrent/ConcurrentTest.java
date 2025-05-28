package org.example.concurrent;

import org.example.datastructure.CustomList;
import org.example.datastructure.CustomQueue;
import org.example.datastructure.CustomStack;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// Clase para probar la funcionalidad de concurrencia.

public class ConcurrentTest {
    
    /**
     * Método principal para probar la concurrencia.
     * @param args Argumentos de línea de comandos (no utilizados)
     * @throws InterruptedException Si el hilo es interrumpido
     * @throws ExecutionException Si ocurre un error durante la ejecución
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Crear el gestor de hilos
        ThreadPoolManager threadPoolManager = new ThreadPoolManager();
        
        System.out.println("Probando tareas asíncronas con estructuras de datos personalizadas");
        
        // Ejemplo 1: Tarea rápida con CustomList
        Future<CustomList<String>> listFuture = threadPoolManager.submitFastTask(
            new AsyncTask<>("create-list", () -> {
                CustomList<String> list = new CustomList<>();
                for (int i = 0; i < 5; i++) {
                    list.add("Item " + i);
                    Thread.sleep(100); // Simular trabajo
                }
                return list;
            })
        );
        
        // Ejemplo 2: Tarea larga con CustomStack
        Future<CustomStack<Integer>> stackFuture = threadPoolManager.submitLongTask(
            new AsyncTask<>("create-stack", () -> {
                CustomStack<Integer> stack = new CustomStack<>();
                for (int i = 0; i < 5; i++) {
                    stack.push(i * 10);
                    Thread.sleep(200); // Simular trabajo más largo
                }
                return stack;
            })
        );
        
        // Ejemplo 3: Tarea programada con CustomQueue
        final CustomQueue<Double> queue = new CustomQueue<>();
        ScheduledFuture<?> scheduledTask = threadPoolManager.scheduleAtFixedRate(
            () -> {
                double value = Math.random() * 100;
                System.out.println("Añadiendo a la cola: " + value);
                queue.enqueue(value);
                
                // Mostrar el contenido actual de la cola
                System.out.println("Cola actual: " + queue);
                
                // Si la cola tiene más de 5 elementos, eliminar el más antiguo
                if (queue.size() > 5) {
                    double removed = queue.dequeue();
                    System.out.println("Eliminando de la cola: " + removed);
                }
            },
            1, // Retraso inicial de 1 segundo
            1, // Ejecutar cada 1 segundo
            TimeUnit.SECONDS
        );
        
        // Esperar a que las tareas asíncronas terminen
        CustomList<String> list = listFuture.get();
        CustomStack<Integer> stack = stackFuture.get();
        
        System.out.println("\nResultados de tareas asíncronas:");
        System.out.println("Lista: " + list);
        System.out.println("Pila: " + stack);
        
        // Dejar que la tarea programada se ejecute durante un tiempo
        System.out.println("\nLa tarea programada se ejecutará durante 10 segundos...");
        Thread.sleep(10000);
        
        // Cancelar la tarea programada
        scheduledTask.cancel(false);
        
        // Apagar el gestor de hilos
        threadPoolManager.shutdown();
        
        System.out.println("\nPrueba de concurrencia completada");
    }
}
