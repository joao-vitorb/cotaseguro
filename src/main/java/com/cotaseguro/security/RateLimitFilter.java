package com.cotaseguro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final LoginRateLimiter loginRateLimiter;
    private final HttpErrorWriter httpErrorWriter;

    public RateLimitFilter(LoginRateLimiter loginRateLimiter, HttpErrorWriter httpErrorWriter) {
        this.loginRateLimiter = loginRateLimiter;
        this.httpErrorWriter = httpErrorWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isLoginRequest(request) && !loginRateLimiter.tryAcquire(clientKey(request))) {
            httpErrorWriter.write(
                    response, HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts", request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI());
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
