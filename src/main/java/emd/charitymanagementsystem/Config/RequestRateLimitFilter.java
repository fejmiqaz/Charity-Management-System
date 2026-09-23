package emd.charitymanagementsystem.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_NANOS = Duration.ofMinutes(1).toNanos();
    private static final int MAX_KEYS = 100_000;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanup = new AtomicLong(System.nanoTime());

    @Value("${app.rate-limit.trust-proxy:false}")
    private boolean trustProxy;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();
        String client = clientAddress(request);
        long now = System.nanoTime();

        // Apply the general quota to every request, including static resources and authentication.
        if (!allow("all:" + client, 300, now)
                || ("POST".equals(method) && ("/login".equals(path) || "/api/auth/login".equals(path)) && !allow("login:" + client, 10, now))
                || ("POST".equals(method) && ("/register".equals(path) || "/api/auth/register".equals(path)) && !allow("register:" + client, 5, now))
                || (isWrite(method) && !("/login".equals(path) || "/api/auth/login".equals(path)) && !("/register".equals(path) || "/api/auth/register".equals(path))
                    && !allow("write:" + client, 60, now))) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            if (path.startsWith("/api/")) {
                emd.charitymanagementsystem.Api.ApiError.write(response, 429);
            } else {
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Too many requests. Please try again in a minute.");
            }
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isWrite(String method) {
        return !"GET".equals(method) && !"HEAD".equals(method) && !"OPTIONS".equals(method);
    }

    private String clientAddress(HttpServletRequest request) {
        if (trustProxy) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String first = forwarded.split(",", 2)[0].trim();
                if (first.matches("[0-9a-fA-F:.]{3,45}")) {
                    return first;
                }
            }
        }
        return request.getRemoteAddr();
    }

    private boolean allow(String key, int limit, long now) {
        long previous = lastCleanup.get();
        if (now - previous >= WINDOW_NANOS && lastCleanup.compareAndSet(previous, now)) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().start > WINDOW_NANOS * 2);
        }
        if (windows.size() >= MAX_KEYS) {
            if (!windows.containsKey(key)) {
                return false;
            }
        }
        Window window = windows.computeIfAbsent(key, ignored -> new Window(now));
        synchronized (window) {
            if (now - window.start >= WINDOW_NANOS) {
                window.start = now;
                window.count = 0;
            }
            return ++window.count <= limit;
        }
    }

    private static final class Window {
        private long start;
        private int count;

        private Window(long start) {
            this.start = start;
        }
    }
}
