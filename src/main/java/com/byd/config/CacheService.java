package com.byd.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    private boolean isRedisAvailable() {
        try {
            if (redisTemplate == null || redisConnectionFactory == null) return false;
            redisConnectionFactory.getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void put(String key, Object value) {
        if (!isRedisAvailable()) return;
        try {
            redisTemplate.opsForValue().set(key, value, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis写入失败: {}", e.getMessage());
        }
    }

    public void put(String key, Object value, long minutes) {
        if (!isRedisAvailable()) return;
        try {
            redisTemplate.opsForValue().set(key, value, minutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis写入失败: {}", e.getMessage());
        }
    }

    public Object get(String key) {
        if (!isRedisAvailable()) return null;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis读取失败: {}", e.getMessage());
            return null;
        }
    }

    public void delete(String key) {
        if (!isRedisAvailable()) return;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis删除失败: {}", e.getMessage());
        }
    }

    public void deleteByPrefix(String prefix) {
        if (!isRedisAvailable()) return;
        try {
            var keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis删除失败: {}", e.getMessage());
        }
    }
}
