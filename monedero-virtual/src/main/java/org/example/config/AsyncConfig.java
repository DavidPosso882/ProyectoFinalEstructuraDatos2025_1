package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration class to enable asynchronous processing.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // This class enables asynchronous processing for methods annotated with @Async
    // Additional configuration can be added here if needed
}
