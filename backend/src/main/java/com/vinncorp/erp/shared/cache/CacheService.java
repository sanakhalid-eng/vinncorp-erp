package com.vinncorp.erp.shared.cache;

import java.util.Optional;

public interface CacheService {
    <T> Optional<T> get(String key);
    <T> Optional<T> get(String key, Class<T> type);
    <T> void put(String key, T value, long ttlMillis);
    void evict(String key);
    void evictByPrefix(String prefix);
    boolean exists(String key);
}

