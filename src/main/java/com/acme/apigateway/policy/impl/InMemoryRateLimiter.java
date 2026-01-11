package com.acme.apigateway.policy.impl;

import com.acme.apigateway.policy.RateLimitConfig;
import com.acme.apigateway.policy.RateLimitResult;
import com.acme.apigateway.policy.RateLimiter;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryRateLimiter implements RateLimiter {
  private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

  @Override
  public Mono<RateLimitResult> allow(String key, RateLimitConfig config) {
    TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(config.capacity(), config.refillPerSecond()));
    synchronized (bucket) {
      bucket.refill();
      if (bucket.tokens > 0) {
        bucket.tokens--;
        return Mono.just(RateLimitResult.allowed(bucket.tokens));
      }
      return Mono.just(RateLimitResult.denied());
    }
  }

  @Override
  public String storageType() {
    return "memory";
  }

  private static class TokenBucket {
    private int tokens;
    private final int capacity;
    private final int refillPerSecond;
    private Instant lastRefill;

    private TokenBucket(int capacity, int refillPerSecond) {
      this.capacity = capacity;
      this.refillPerSecond = refillPerSecond;
      this.tokens = capacity;
      this.lastRefill = Instant.now();
    }

    private void refill() {
      Instant now = Instant.now();
      long seconds = Math.max(0, now.getEpochSecond() - lastRefill.getEpochSecond());
      if (seconds > 0) {
        long refill = seconds * refillPerSecond;
        tokens = (int) Math.min(capacity, tokens + refill);
        lastRefill = now;
      }
    }
  }
}
