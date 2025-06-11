package com.dentistdss.reporting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Asynchronous Processing Configuration
 * 
 * Configures thread pools for different types of async operations:
 * - Report generation (CPU intensive)
 * - Email delivery (I/O intensive)
 * - Data analytics (mixed workload)
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${reporting.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${reporting.async.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${reporting.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${reporting.async.thread-name-prefix:reporting-}")
    private String threadNamePrefix;

    /**
     * Primary executor for report generation tasks
     */
    @Bean(name = "reportGenerationExecutor")
    public Executor reportGenerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix + "generation-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("Configured report generation executor - Core: {}, Max: {}, Queue: {}", 
                corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }

    /**
     * Executor for email delivery tasks
     */
    @Bean(name = "emailDeliveryExecutor")
    public Executor emailDeliveryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix(threadNamePrefix + "email-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("Configured email delivery executor - Core: 3, Max: 10, Queue: 50");
        return executor;
    }

    /**
     * Executor for data analytics tasks
     */
    @Bean(name = "analyticsExecutor")
    public Executor analyticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix(threadNamePrefix + "analytics-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // Longer timeout for analytics
        executor.initialize();
        
        log.info("Configured analytics executor - Core: 4, Max: 12, Queue: 25");
        return executor;
    }

    /**
     * Executor for scheduled tasks
     */
    @Bean(name = "scheduledTaskExecutor")
    public Executor scheduledTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix(threadNamePrefix + "scheduled-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("Configured scheduled task executor - Core: 2, Max: 5, Queue: 20");
        return executor;
    }
}
