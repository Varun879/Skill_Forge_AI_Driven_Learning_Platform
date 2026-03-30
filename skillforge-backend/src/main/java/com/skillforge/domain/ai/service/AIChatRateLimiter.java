package com.skillforge.domain.ai.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.skillforge.exception.TooManyRequestsException;

@Service
public class AIChatRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final int perMinute;
    private final Map<String, LocalCounter> localCounters = new ConcurrentHashMap<>();

    public AIChatRateLimiter(
            @org.springframework.lang.Nullable StringRedisTemplate redisTemplate,
            @Value("${ai.chat.rate-limit.per-minute:20}") int perMinute) {
        this.redisTemplate = redisTemplate;
        this.perMinute = perMinute;
    }

    public void assertAllowed(Long userId) {
        String key = "skillforge:ratelimit:ai-chat:%s".formatted(userId);
        long count = increment(key);
        if (count > perMinute) {
            throw new TooManyRequestsException("AI chat rate limit exceeded. Please retry in a minute.");
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
            if (v == null || now - v.windowStartMs >= 60_000L) {
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
