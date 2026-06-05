package com.tutorassist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    private final Map<String, RateLimit> rateLimits = new ConcurrentHashMap<>();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/auth/login");
    }

    @Bean
    public HandlerInterceptor rateLimitInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                String clientIp = getClientIp(request);
                String key = clientIp + ":" + System.currentTimeMillis() / 60000; // 每分钟

                RateLimit limit = rateLimits.computeIfAbsent(key, k -> new RateLimit());
                if (limit.count.incrementAndGet() > 100) { // 每分钟最多100次请求
                    response.setStatus(429);
                    response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
                    return false;
                }

                // 清理过期的限流记录
                if (rateLimits.size() > 1000) {
                    long currentMinute = System.currentTimeMillis() / 60000;
                    rateLimits.entrySet().removeIf(entry -> {
                        String entryKey = entry.getKey();
                        long entryMinute = Long.parseLong(entryKey.split(":")[1]);
                        return currentMinute - entryMinute > 5;
                    });
                }

                return true;
            }
        };
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static class RateLimit {
        AtomicInteger count = new AtomicInteger(0);
    }
}
