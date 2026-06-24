package com.cotaseguro.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {

    private final boolean enabled;
    private final int maxAttempts;
    private final long windowSeconds;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Autowired
    public LoginRateLimiter(
            @Value("${app.security.rate-limit.login.enabled:true}") boolean enabled,
            @Value("${app.security.rate-limit.login.max-attempts:5}") int maxAttempts,
            @Value("${app.security.rate-limit.login.window-seconds:60}") long windowSeconds) {
        this(enabled, maxAttempts, windowSeconds, Clock.systemUTC());
    }

    LoginRateLimiter(boolean enabled, int maxAttempts, long windowSeconds, Clock clock) {
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
        this.clock = clock;
    }

    public boolean tryAcquire(String key) {
        if (!enabled) {
            return true;
        }

        Instant now = clock.instant();
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || current.isExpired(now, windowSeconds)) {
                return new Window(now, 1);
            }
            return current.increment();
        });

        return window.count() <= maxAttempts;
    }

    private record Window(Instant startedAt, int count) {

        boolean isExpired(Instant now, long windowSeconds) {
            return now.isAfter(startedAt.plusSeconds(windowSeconds));
        }

        Window increment() {
            return new Window(startedAt, count + 1);
        }

    }

}
