package com.skillforge.domain.execution.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisExecutionQueue implements ExecutionQueue {

    private final StringRedisTemplate redisTemplate;
    private final String queueKey;

    public RedisExecutionQueue(
            StringRedisTemplate redisTemplate,
            @Value("${execution.queue.key}") String queueKey) {
        this.redisTemplate = redisTemplate;
        this.queueKey = queueKey;
    }

    @Override
    public void enqueue(Long executionId) {
        redisTemplate.opsForList().rightPush(queueKey, executionId.toString());
    }

    @Override
    public List<Long> drain(int maxItems) {
        int batchSize = Math.max(maxItems, 1);
        List<Long> drained = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            String item = redisTemplate.opsForList().leftPop(queueKey);
            if (item == null) {
                break;
            }
            drained.add(Long.parseLong(item));
        }
        return drained;
    }
}
