package com.justen.auth.domain.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.justen.infrastructure.AppProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de Rate Limiting baseado no algoritmo Token Bucket / Contador de Janela Fixa.
 * Projetado para suportar fácil transição para Redis em clusters distribuídos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitingService {

    private final AppProperties appProperties;

    // Cache local em memória (thread-safe). Em produção com múltiplas instâncias,
    // substitui-se por RedisTemplate / Redisson usando chaves com TTL de 60s.
    private final Map<String, RateLimitCounter> limitCounters = new ConcurrentHashMap<>();

    public boolean tryAcquire(String clientIp, String action) {
        if (!Boolean.TRUE.equals(appProperties.getAuth().getRateLimit().getEnabled())) {
            return true;
        }

        int maxRequests;
        if ("login".equalsIgnoreCase(action) || "mfa".equalsIgnoreCase(action)) {
            maxRequests = appProperties.getAuth().getRateLimit().getLoginRequestsPerMinute() != null
                    ? appProperties.getAuth().getRateLimit().getLoginRequestsPerMinute()
                    : 15;
        } else {
            maxRequests = appProperties.getAuth().getRateLimit().getRequestsPerMinute() != null
                    ? appProperties.getAuth().getRateLimit().getRequestsPerMinute()
                    : 60;
        }

        String key = action + ":" + (clientIp != null ? clientIp : "unknown");
        long currentMinute = System.currentTimeMillis() / 60000;

        RateLimitCounter counter = limitCounters.compute(key, (k, existing) -> {
            if (existing == null || existing.minute != currentMinute) {
                return new RateLimitCounter(currentMinute, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        boolean allowed = counter.count.get() <= maxRequests;
        if (!allowed) {
            log.warn("Rate limit exceeded for key: {} (count={}, limit={})", key, counter.count.get(), maxRequests);
        }
        return allowed;
    }

    private static class RateLimitCounter {
        final long minute;
        final AtomicInteger count;

        RateLimitCounter(long minute, AtomicInteger count) {
            this.minute = minute;
            this.count = count;
        }
    }
}
