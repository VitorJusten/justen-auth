package com.justen.auth.core.security.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.justen.auth.domain.service.RateLimitingService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filtro HTTP para proteção contra ataques de força bruta, credential stuffing e abuso via Rate Limiting
 */
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method) && isRateLimitedPath(path)) {
            String clientIp = getClientIp(request);
            String action = getActionName(path);

            boolean allowed = rateLimitingService.tryAcquire(clientIp, action);
            if (!allowed) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", "60");
                response.getWriter().write("""
                    {
                        "status": 429,
                        "title": "Too Many Requests. Please slow down and try again in 1 minute."
                    }
                """);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedPath(String path) {
        return path.startsWith("/auth/login") ||
               path.startsWith("/auth/mfa/verify") ||
               path.startsWith("/oauth2/token") ||
               path.startsWith("/auth/refresh");
    }

    private String getActionName(String path) {
        if (path.contains("login")) return "login";
        if (path.contains("mfa")) return "mfa";
        if (path.contains("token")) return "token";
        return "general";
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
