package com.skillforge.domain.execution.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.skillforge.exception.TooManyRequestsException;

@Service
public class ExecutionRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final int runPerMinute;
    private final int submitPerMinute;
    private final Map<String, LocalCounter> localCounters = new ConcurrentHashMap<>();

    public ExecutionRateLimiter(
            @org.springframework.lang.Nullable StringRedisTemplate redisTemplate,
            @Value("${execution.rate-limit.run-per-minute}") int runPerMinute,
            @Value("${execution.rate-limit.submit-per-minute}") int submitPerMinute) {
        this.redisTemplate = redisTemplate;
        this.runPerMinute = runPerMinute;
        this.submitPerMinute = submitPerMinute;
    }

    public void assertRunAllowed(Long userId) {
        assertAllowed("run", userId, runPerMinute);
    }

    public void assertSubmitAllowed(Long userId) {
        assertAllowed("submit", userId, submitPerMinute);
    }

    private void assertAllowed(String action, Long userId, int limit) {
        String key = "skillforge:ratelimit:%s:%s".formatted(action, userId);
        long count = increment(key);
        if (count > limit) {
            throw new TooManyRequestsException("Rate limit exceeded for " + action + ". Please retry in a minute.");
        }
    }

    private long increment(String key) {
        if (redisTemplate != null) {
            try {
                Long value = redisTemplate.opsForValue().increment(key);
                if (value != null && value == 1L) {
                    redisTemplate.expire(key, Duration.ofMinutes(1));
                }
                return value == null ? 1L : value;
            } catch (Exception ignored) {
            }
        }

        LocalCounter counter = localCounters.compute(key, (k, v) -> {
            long now = System.currentTimeMillis();
            if (v == null || now - v.windowStartMs >= 60_000) {
                return new LocalCounter(now, 1L);
            }
            v.count++;
            return v;
        });
        return counter.count;
    }

    private static final class LocalCounter {
        private final long windowStartMs;
        private long count;

        private LocalCounter(long windowStartMs, long count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}
