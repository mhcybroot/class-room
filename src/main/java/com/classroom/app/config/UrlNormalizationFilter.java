package com.classroom.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UrlNormalizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String normalizedUri = requestUri.replaceAll("/{2,}", "/");

        if (!requestUri.equals(normalizedUri)) {
            String query = request.getQueryString();
            String location = normalizedUri + (query == null || query.isBlank() ? "" : "?" + query);
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", location);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
