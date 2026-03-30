package com.skillforge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.skillforge.domain.execution.service.ExecutionQueue;
import com.skillforge.domain.execution.service.InMemoryExecutionQueue;
import com.skillforge.domain.execution.service.RedisExecutionQueue;

@Configuration
public class ExecutionQueueConfig {

    @Bean
    public ExecutionQueue executionQueue(
            @Value("${execution.queue.redis-enabled:false}") boolean redisEnabled,
            @Value("${execution.queue.key}") String queueKey,
            @org.springframework.lang.Nullable StringRedisTemplate redisTemplate) {
        if (redisEnabled && redisTemplate != null) {
            return new RedisExecutionQueue(redisTemplate, queueKey);
        }
        return new InMemoryExecutionQueue();
    }
}
