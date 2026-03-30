package com.skillforge.domain.execution.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CompilationCacheService {

    private final StringRedisTemplate redisTemplate;
    private final int ttlSeconds;
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();

    public CompilationCacheService(
            @org.springframework.lang.Nullable StringRedisTemplate redisTemplate,
            @Value("${execution.cache.ttl-seconds}") int ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }

    public String getCompilationError(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.opsForValue().get(redisKey(key));
            } catch (Exception ignored) {
            }
        }

        CacheEntry entry = localCache.get(key);
        if (entry == null || entry.expiresAtMs < System.currentTimeMillis()) {
            localCache.remove(key);
            return null;
        }
        return entry.stderr;
    }

    public void putCompilationError(String key, String stderr) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(redisKey(key), stderr, Duration.ofSeconds(ttlSeconds));
                return;
            } catch (Exception ignored) {
            }
        }

        localCache.put(key, new CacheEntry(stderr, System.currentTimeMillis() + ttlSeconds * 1000L));
    }

    private String redisKey(String key) {
        return "skillforge:compilecache:" + key;
    }

    private record CacheEntry(String stderr, long expiresAtMs) {}
}
