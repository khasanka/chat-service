package com.example.chatservice.rate;


import com.example.chatservice.config.AppProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private final AppProperties props;
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    if (!path.startsWith("/api/")) {
      filterChain.doFilter(request, response);
      return;
    }

    String apiKey = request.getHeader("X-API-KEY");
    if (apiKey == null || apiKey.isBlank()) {
      filterChain.doFilter(request, response);
      return;
    }

    var bucket = buckets.computeIfAbsent(apiKey, this::newBucket);
    var probe = bucket.tryConsumeAndReturnRemaining(1);
    response.setHeader("X-RateLimit-Remaining", Long.toString(Math.max(0, probe.getRemainingTokens())));
    if (!probe.isConsumed()) {
      long nanosToWait = probe.getNanosToWaitForRefill();
      long seconds = Duration.ofNanos(nanosToWait).toSeconds();
      response.setHeader("Retry-After", Long.toString(Math.max(1, seconds)));
      response.sendError(429, "rate_limited");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private Bucket newBucket(String key) {
    int capacity = Math.max(1, props.getRateLimit().getCapacity());
    int refill = Math.max(1, props.getRateLimit().getRefillPerMinute());
    Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(refill, Duration.ofMinutes(1)));
    return Bucket.builder().addLimit(limit).build();
  }
}
