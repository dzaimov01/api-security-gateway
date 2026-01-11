package com.acme.apigateway.policy;

import reactor.core.publisher.Mono;

public interface RateLimiter {
  Mono<RateLimitResult> allow(String key, RateLimitConfig config);
  String storageType();
}
