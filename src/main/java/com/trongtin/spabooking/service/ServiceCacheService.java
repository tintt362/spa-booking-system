package com.trongtin.spabooking.service;

import com.trongtin.spabooking.dto.response.ServiceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration TTL = Duration.ofMinutes(30);

    private static final String KEY_ALL = "service:all";
    private static final String KEY_ID = "service:id:";

    public List<ServiceDTO> getAll() {
        try {
            Object data = redisTemplate.opsForValue().get(KEY_ALL);
            if (data != null) {
                log.info("Cache HIT: service:all");
                return (List<ServiceDTO>) data;
            }
        } catch (Exception e) {
            log.warn("Redis error: {}", e.getMessage());
        }
        return null;
    }

    public ServiceDTO getById(Long id) {
        try {
            Object data = redisTemplate.opsForValue().get(KEY_ID + id);
            if (data != null) {
                log.info("Cache HIT: service:id:{}", id);
                return (ServiceDTO) data;
            }
        } catch (Exception e) {
            log.warn("Redis error: {}", e.getMessage());
        }
        return null;
    }

    public void setAll(List<ServiceDTO> services) {
        try {
            redisTemplate.opsForValue().set(KEY_ALL, services, TTL);
        } catch (Exception e) {
            log.warn("Redis error: {}", e.getMessage());
        }
    }

    public void setById(Long id, ServiceDTO dto) {
        try {
            redisTemplate.opsForValue().set(KEY_ID + id, dto, TTL);
        } catch (Exception e) {
            log.warn("Redis error: {}", e.getMessage());
        }
    }


    public void evictAll() {
        redisTemplate.delete(KEY_ALL);
    }

    public void evictById(Long id) {
        redisTemplate.delete(KEY_ID + id);
    }

    public void evictAllPattern() {
        Set<String> keys = redisTemplate.keys("service:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}