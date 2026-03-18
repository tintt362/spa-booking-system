package com.trongtin.spabooking.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "blacklist:";

    public void blacklistToken(String jti, long ttl) {
        redisTemplate.opsForValue().set(PREFIX + jti, "revoked", ttl, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String jti) {
        log.warn("isBlacklisted : " + redisTemplate.hasKey(PREFIX + jti));
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
    }
}