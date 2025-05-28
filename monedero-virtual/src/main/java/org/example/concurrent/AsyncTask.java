package org.example.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

 //Esta es una clase genérica para representar una tarea asíncrona.

public class AsyncTask<T> implements Callable<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncTask.class);
    
    private final String taskName;
    private final Callable<T> task;
    private final Consumer<T> onSuccess;
    private final Consumer<Throwable> onError;
    
    /**
     * Constructor para una tarea asíncrona.
     * @param taskName Nombre de la tarea (para logging)
     * @param task Tarea a ejecutar
     * @param onSuccess Callback a ejecutar en caso de éxito
     * @param onError Callback a ejecutar en caso de error
     */
    public AsyncTask(String taskName, Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        this.taskName = taskName;
        this.task = task;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }
    
    /**
     * Constructor para una tarea asíncrona sin callbacks.
     * @param taskName Nombre de la tarea (para logging)
     * @param task Tarea a ejecutar
     */
    public AsyncTask(String taskName, Callable<T> task) {
        this(taskName, task, null, null);
    }
    
    @Override
    public T call() throws Exception {
        logger.debug("Starting async task: {}", taskName);
        long startTime = System.currentTimeMillis();
        
        try {
            T result = task.call();
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Completed async task: {} in {} ms", taskName, duration);
            
            if (onSuccess != null) {
                try {
                    onSuccess.accept(result);
                } catch (Exception e) {
                    logger.error("Error in onSuccess callback for task: {}", taskName, e);
                }
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed async task: {} after {} ms", taskName, duration, e);
            
            if (onError != null) {
                try {
                    onError.accept(e);
                } catch (Exception callbackError) {
                    logger.error("Error in onError callback for task: {}", taskName, callbackError);
                }
            }
            
            throw e;
        }
    }
    
    /**
     * Crea una tarea asíncrona a partir de un Runnable.
     * @param taskName Nombre de la tarea
     * @param runnable Tarea a ejecutar
     * @param onSuccess Callback a ejecutar en caso de éxito
     * @param onError Callback a ejecutar en caso de error
     * @return AsyncTask que ejecuta el Runnable
     */
    public static AsyncTask<Void> fromRunnable(String taskName, Runnable runnable, 
                                              Runnable onSuccess, Consumer<Throwable> onError) {
        return new AsyncTask<>(
            taskName,
            () -> {
                runnable.run();
                return null;
            },
            result -> {
                if (onSuccess != null) {
                    onSuccess.run();
                }
            },
            onError
        );
    }
    
    /**
     * Crea una tarea asíncrona a partir de un Runnable sin callbacks.
     * @param taskName Nombre de la tarea
     * @param runnable Tarea a ejecutar
     * @return AsyncTask que ejecuta el Runnable
     */
    public static AsyncTask<Void> fromRunnable(String taskName, Runnable runnable) {
        return fromRunnable(taskName, runnable, null, null);
    }
}
