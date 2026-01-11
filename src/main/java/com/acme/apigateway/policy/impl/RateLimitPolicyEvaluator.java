package com.acme.apigateway.policy.impl;

import com.acme.apigateway.config.GatewayProperties;
import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.policy.PolicyConfigParser;
import com.acme.apigateway.policy.PolicyContext;
import com.acme.apigateway.policy.PolicyDecision;
import com.acme.apigateway.policy.PolicyEvaluator;
import com.acme.apigateway.policy.RateLimitConfig;
import com.acme.apigateway.policy.RateLimiter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RateLimitPolicyEvaluator implements PolicyEvaluator {
  private final PolicyConfigParser parser;
  private final RateLimiter rateLimiter;
  private final GatewayProperties properties;

  public RateLimitPolicyEvaluator(final PolicyConfigParser parser, final List<RateLimiter> limiters,
      final GatewayProperties properties) {
    this.parser = parser;
    this.properties = properties;
    this.rateLimiter = limiters.stream()
        .filter(limiter -> limiter.storageType().equalsIgnoreCase(properties.rateLimit().storage()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No rate limiter configured"));
  }

  @Override
  public String handlesType() {
    return "rate_limit";
  }

  @Override
  public Mono<PolicyDecision> evaluate(final PolicyEntity policy, final PolicyContext context) {
    RateLimitConfig config = parser.parse(policy.configJson(), RateLimitConfig.class);
    String key = buildKey(config.scope(), context);
    return rateLimiter.allow(key, normalize(config))
        .map(result -> {
          if (result.allowed()) {
            return PolicyDecision.allow(handlesType());
          }
          return PolicyDecision.deny(handlesType(), "rate_limit_exceeded",
              Map.of("status", 429, "remaining", result.remaining()));
        });
  }

  private RateLimitConfig normalize(final RateLimitConfig config) {
    int capacity = config.capacity() > 0 ? config.capacity() : properties.rateLimit().defaultCapacity();
    int refill = config.refillPerSecond() > 0 ? config.refillPerSecond() : properties.rateLimit().defaultRefillPerSecond();
    long window = config.windowSeconds() > 0 ? config.windowSeconds() : properties.rateLimit().defaultWindow().getSeconds();
    return new RateLimitConfig(config.scope(), capacity, refill, window);
  }

  private String buildKey(final String scope, final PolicyContext context) {
    String normalizedScope = scope == null ? "ip" : scope;
    return switch (normalizedScope) {
      case "api_key" -> "api:" + context.apiKey();
      case "user" -> "user:" + context.request().getHeaders().getFirst("X-User-Id");
      default -> "ip:" + context.clientIp();
    };
  }
}
