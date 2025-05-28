package org.example.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


 // Gestor de hilos personalizado para la aplicación.
 // Proporciona pools de hilos para diferentes tipos de tareas.

@Component
public class ThreadPoolManager {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);

    // Pool para tareas rápidas (como envío de notificaciones)
    private final ExecutorService fastTaskPool;

    // Pool para tareas que pueden tardar más (como análisis de patrones)
    private final ExecutorService longTaskPool;

    // Pool para tareas programadas (como transacciones programadas)
    private final ScheduledExecutorService scheduledTaskPool;

    public ThreadPoolManager() {
        // Configuración de los pools
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // Para tareas rápidas, usamos más hilos
        fastTaskPool = new ThreadPoolExecutor(
                availableProcessors,
                availableProcessors * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new CustomThreadFactory("fast-task-"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        // Para tareas largas, limitamos el número de hilos
        longTaskPool = new ThreadPoolExecutor(
                availableProcessors / 2,
                availableProcessors,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(20),
                new CustomThreadFactory("long-task-"),
                new ThreadPoolExecutor.AbortPolicy());

        // Para tareas programadas
        scheduledTaskPool = Executors.newScheduledThreadPool(
                availableProcessors,
                new CustomThreadFactory("scheduled-task-"));

        logger.info("Thread pools initialized with {} available processors", availableProcessors);
    }

    /**
     * Ejecuta una tarea rápida de forma asíncrona.
     * @param task Tarea a ejecutar
     * @return Future que representa el resultado de la tarea
     */
    public <T> Future<T> submitFastTask(Callable<T> task) {
        return fastTaskPool.submit(task);
    }

    /**
     * Ejecuta una tarea rápida de forma asíncrona.
     * @param task Tarea a ejecutar
     */
    public void executeFastTask(Runnable task) {
        fastTaskPool.execute(task);
    }

    /**
     * Ejecuta una tarea larga de forma asíncrona.
     * @param task Tarea a ejecutar
     * @return Future que representa el resultado de la tarea
     */
    public <T> Future<T> submitLongTask(Callable<T> task) {
        return longTaskPool.submit(task);
    }

    /**
     * Ejecuta una tarea larga de forma asíncrona.
     * @param task Tarea a ejecutar
     */
    public void executeLongTask(Runnable task) {
        longTaskPool.execute(task);
    }

    /**
     * Programa una tarea para ejecutarse después de un retraso.
     * @param task Tarea a ejecutar
     * @param delay Retraso antes de la ejecución
     * @param unit Unidad de tiempo del retraso
     * @return ScheduledFuture que representa la tarea programada
     */
    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit unit) {
        return scheduledTaskPool.schedule(task, delay, unit);
    }

    /**
     * Programa una tarea para ejecutarse periódicamente.
     * @param task Tarea a ejecutar
     * @param initialDelay Retraso inicial antes de la primera ejecución
     * @param period Período entre ejecuciones sucesivas
     * @param unit Unidad de tiempo del retraso y período
     * @return ScheduledFuture que representa la tarea programada
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledTaskPool.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * Programa una tarea para ejecutarse periódicamente con un retraso fijo entre el final de una ejecución y el inicio de la siguiente.
     * @param task Tarea a ejecutar
     * @param initialDelay Retraso inicial antes de la primera ejecución
     * @param delay Retraso entre el final de una ejecución y el inicio de la siguiente
     * @param unit Unidad de tiempo del retraso
     * @return ScheduledFuture que representa la tarea programada
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return scheduledTaskPool.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    //Cierra los pools de hilos de forma ordenada.
    
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down thread pools");

        // Primero intentamos un apagado ordenado
        fastTaskPool.shutdown();
        longTaskPool.shutdown();
        scheduledTaskPool.shutdown();

        try {
            // Esperamos a que terminen las tareas en curso
            if (!fastTaskPool.awaitTermination(10, TimeUnit.SECONDS)) {
                fastTaskPool.shutdownNow();
            }
            if (!longTaskPool.awaitTermination(20, TimeUnit.SECONDS)) {
                longTaskPool.shutdownNow();
            }
            if (!scheduledTaskPool.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduledTaskPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Restauramos el flag de interrupción
            Thread.currentThread().interrupt();

            // Forzamos el apagado
            fastTaskPool.shutdownNow();
            longTaskPool.shutdownNow();
            scheduledTaskPool.shutdownNow();

            logger.error("Thread pools shutdown interrupted", e);
        }

        logger.info("Thread pools shutdown completed");
    }


    //Fábrica de hilos personalizada para nombrar los hilos.
    private static class CustomThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            // Configuramos como hilos daemon para que no impidan el apagado de la JVM
            thread.setDaemon(true);
            // Prioridad normal
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }
}
