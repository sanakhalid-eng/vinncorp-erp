package com.vinncorp.erp.shared.cache;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Primary
@Slf4j
public class RedisCacheService implements CacheService {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private final ConcurrentHashMap<String, CacheEntry<?>> fallbackCache = new ConcurrentHashMap<>();
    @Setter
    private volatile boolean redisAvailable = true;

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

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        if (redisAvailable && redisTemplate != null) {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    return Optional.of((T) value);
                }
            } catch (Exception e) {
                log.warn("Redis get failed for key={}, falling back to in-memory", key);
                redisAvailable = false;
            }
        }
        return getFromFallback(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        if (redisAvailable && redisTemplate != null) {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    if (type.isInstance(value)) {
                        return Optional.of(type.cast(value));
                    }
                    return Optional.of((T) value);
                }
            } catch (Exception e) {
                log.warn("Redis get failed for key={}, falling back to in-memory", key);
                redisAvailable = false;
            }
        }
        return getFromFallback(key);
    }

    @Override
    public <T> void put(String key, T value, long ttlMillis) {
        if (value == null) {
            evict(key);
            return;
        }

        if (redisAvailable && redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.warn("Redis put failed for key={}, falling back to in-memory", key);
                redisAvailable = false;
                putToFallback(key, value, ttlMillis);
            }
        } else {
            putToFallback(key, value, ttlMillis);
        }
    }

    @Override
    public void evict(String key) {
        if (redisAvailable && redisTemplate != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis evict failed for key={}", key);
                redisAvailable = false;
            }
        }
        fallbackCache.remove(key);
    }

    @Override
    public void evictByPrefix(String prefix) {
        if (redisAvailable && redisTemplate != null) {
            try {
                Set<String> keys = redisTemplate.keys(prefix + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                log.warn("Redis evictByPrefix failed for prefix={}", prefix);
                redisAvailable = false;
            }
        }
        fallbackCache.keySet().removeIf(k -> k.startsWith(prefix));
    }

    @Override
    public boolean exists(String key) {
        if (redisAvailable && redisTemplate != null) {
            try {
                return Boolean.TRUE.equals(redisTemplate.hasKey(key));
            } catch (Exception e) {
                log.warn("Redis exists check failed for key={}", key);
                redisAvailable = false;
            }
        }
        return existsInFallback(key);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getFromFallback(String key) {
        CacheEntry<?> entry = fallbackCache.get(key);
        if (entry == null) return Optional.empty();
        if (entry.isExpired()) {
            fallbackCache.remove(key);
            return Optional.empty();
        }
        return Optional.ofNullable((T) entry.value);
    }

    private <T> void putToFallback(String key, T value, long ttlMillis) {
        fallbackCache.put(key, new CacheEntry<>(value, ttlMillis));
    }

    private boolean existsInFallback(String key) {
        CacheEntry<?> entry = fallbackCache.get(key);
        if (entry == null) return false;
        if (entry.isExpired()) {
            fallbackCache.remove(key);
            return false;
        }
        return true;
    }

}

