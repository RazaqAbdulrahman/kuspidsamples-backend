package com.kuspidsamples.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Resolve a bucket for the given key (IP address or user ID)
     * Default: 100 requests per minute
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Resolve bucket for authentication endpoints (stricter)
     * 10 requests per minute
     */
    public Bucket resolveAuthBucket(String key) {
        return cache.computeIfAbsent("auth:" + key, k -> createAuthBucket());
    }

    /**
     * Standard rate limit: 100 requests per minute
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Strict rate limit for authentication: 10 requests per minute
     */
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Check if request is allowed
     */
    public boolean tryConsume(String key) {
        Bucket bucket = resolveBucket(key);
        return bucket.tryConsume(1);
    }

    /**
     * Check if auth request is allowed
     */
    public boolean tryConsumeAuth(String key) {
        Bucket bucket = resolveAuthBucket(key);
        return bucket.tryConsume(1);
    }
}