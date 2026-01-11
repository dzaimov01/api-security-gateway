package com.acme.apigateway.policy;

import java.time.Duration;

public record RateLimitConfig(
    String scope,
    int capacity,
    int refillPerSecond,
    long windowSeconds) {

  public Duration window() {
    return Duration.ofSeconds(windowSeconds);
  }
}
