package com.cartflow.userservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Order(1)
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String method = request.getMethod();
        String uri    = request.getRequestURI();
        long start    = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            if (!uri.startsWith("/actuator")) {
                log.info("[{}] {} {} — {}ms",
                        response.getStatus(), method, uri,
                        System.currentTimeMillis() - start);
            }
        }
    }
}
