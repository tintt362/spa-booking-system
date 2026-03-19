package com.trongtin.spabooking.service;

import com.trongtin.spabooking.dto.response.TherapistDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TherapistCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY = "therapist:all";
    private static final Duration TTL = Duration.ofMinutes(30);

    public List<TherapistDTO> getAll() {
        try {
            Object data = redisTemplate.opsForValue().get(KEY);
            if (data != null) {
                log.info("Cache HIT: therapist:all");
                return (List<TherapistDTO>) data;
            }
        } catch (Exception e) {
            log.warn("Redis error: {}", e.getMessage());
        }
        return null;
    }

    public void setAll(List<TherapistDTO> list) {
        redisTemplate.opsForValue().set(KEY, list, TTL);
    }

    public void evict() {
        redisTemplate.delete(KEY);
    }
}