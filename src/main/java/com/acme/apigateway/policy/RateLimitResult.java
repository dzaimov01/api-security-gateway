package com.acme.apigateway.policy;

public record RateLimitResult(boolean allowed, int remaining) {
  public static RateLimitResult allowed(final int remaining) {
    return new RateLimitResult(true, remaining);
  }

  public static RateLimitResult denied() {
    return new RateLimitResult(false, 0);
  }
}
