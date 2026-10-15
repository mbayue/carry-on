package com.bayue.task_manager.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CacheService {

    private final StringRedisTemplate redisTemplate;

    public CacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeActiveToken(String token, Duration timeout) {
        redisTemplate.opsForValue().set("auth:token:" + token, "1", timeout);
    }

    public boolean isTokenActive(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("auth:token:" + token));
    }

    public void revokeToken(String token) {
        redisTemplate.delete("auth:token:" + token);
    }
}
