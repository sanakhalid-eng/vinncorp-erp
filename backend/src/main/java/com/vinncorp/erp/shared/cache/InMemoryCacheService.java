package com.vinncorp.erp.shared.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fallback cache service used only when RedisCacheService is not available.
 * RedisCacheService has built-in in-memory fallback, so this is rarely used.
 */
@Service
@Slf4j
@ConditionalOnMissingBean(RedisCacheService.class)
public class InMemoryCacheService implements CacheService {

    private static class CacheEntry<T> {
        private final T value;
        private final long expiry;

        CacheEntry(T value, long ttlMillis) {
            this.value = value;
            this.expiry = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiry;
        }
    }

    private final ConcurrentHashMap<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        CacheEntry<?> entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.ofNullable((T) entry.value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        return get(key);
    }

    @Override
    public <T> void put(String key, T value, long ttlMillis) {
        if (value == null) {
            evict(key);
            return;
        }
        cache.put(key, new CacheEntry<>(value, ttlMillis));
    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }

    @Override
    public void evictByPrefix(String prefix) {
        cache.keySet().removeIf(k -> k.startsWith(prefix));
    }

    @Override
    public boolean exists(String key) {
        CacheEntry<?> entry = cache.get(key);
        if (entry == null) return false;
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        return true;
    }
}

