package com.vinncorp.erp.shared.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(2)
@Slf4j
public class RequestTimingFilter implements Filter {

    private static final long SLOW_THRESHOLD_MS = 2000;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String method = httpRequest.getMethod();
            String path = httpRequest.getRequestURI();
            int status = ((HttpServletResponse) response).getStatus();

            if (duration > SLOW_THRESHOLD_MS) {
                log.warn("SLOW_REQUEST: {} {} {} {}ms [{}]", method, path, status, duration, MDC.get("correlationId"));
            }

            log.debug("REQUEST: {} {} {} {}ms", method, path, status, duration);
        }
    }
}

