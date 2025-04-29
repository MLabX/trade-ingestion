package com.magiccode.tradeingestion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    // Lua script for atomic acquire lock operation
    private static final RedisScript<Boolean> ACQUIRE_LOCK_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
        "  redis.call('pexpire', KEYS[1], ARGV[2]) " +
        "  return true " +
        "end " +
        "return false",
        Boolean.class
    );

    // Lua script for atomic release lock operation
    private static final RedisScript<Boolean> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "  redis.call('del', KEYS[1]) " +
        "  return true " +
        "end " +
        "return false",
        Boolean.class
    );

    public boolean acquireLock(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.execute(
            ACQUIRE_LOCK_SCRIPT,
            Collections.singletonList(key),
            value,
            String.valueOf(ttl.toMillis())
        ));
    }

    public boolean releaseLock(String key, String value) {
        return Boolean.TRUE.equals(redisTemplate.execute(
            RELEASE_LOCK_SCRIPT,
            Collections.singletonList(key),
            value
        ));
    }

    public void set(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Boolean setIfAbsent(String key, String value, Duration ttl) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
    }

    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void delete(Set<String> keys) {
        redisTemplate.delete(keys);
    }

    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
} 