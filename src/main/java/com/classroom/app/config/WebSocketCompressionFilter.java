package com.classroom.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class WebSocketCompressionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isWebSocketUpgrade(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("Sec-WebSocket-Extensions".equalsIgnoreCase(name)) {
                    return "";
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if ("Sec-WebSocket-Extensions".equalsIgnoreCase(name)) {
                    return Collections.enumeration(Collections.emptyList());
                }
                return super.getHeaders(name);
            }
        };

        filterChain.doFilter(wrapped, response);
    }

    private boolean isWebSocketUpgrade(HttpServletRequest request) {
        String upgrade = request.getHeader("Upgrade");
        return upgrade != null && "websocket".equalsIgnoreCase(upgrade);
    }
}
