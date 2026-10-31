package com.parksync.backend.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.parksync.backend.exception.UnauthorizedException;
import com.parksync.backend.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    public JwtAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow CORS pre-flight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateToken(token)) {
                Long userId = jwtService.extractUserId(token);
                String email = jwtService.extractEmail(token);
                String role = jwtService.extractRole(token);

                request.setAttribute("currentUserId", userId);
                request.setAttribute("currentUserEmail", email);
                request.setAttribute("currentUserRole", role);
            }
        }

        String uri = request.getRequestURI();

        // Enforce admin role on /api/admin/**
        if (uri.startsWith("/api/admin")) {
            String role = (String) request.getAttribute("currentUserRole");
            if (role == null || !role.equals("ROLE_ADMIN")) {
                throw new UnauthorizedException("Access denied: Administrator privileges required");
            }
        }

        return true;
    }
}
